package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.domain.common.exception.ExternalApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions APIをWebClientで直接呼び出す薄いクライアント（SDK不使用）。
 * {@code POST /v1/chat/completions} を呼び出し、応答メッセージの文字列を返す。
 * <p>
 * OpenAIが障害・過負荷状態になると、リトライ無しでは各リクエストがタイムアウトいっぱいまで
 * ブロックされ続けアプリ全体が詰まってしまうため、{@code @Retry}(一時的な失敗を数回再試行)と
 * {@code @CircuitBreaker}(失敗が続くと一定時間即座に失敗させ、無駄な待機を防ぐ)を適用している。
 * アノテーションはSpring AOPプロキシ経由でのみ有効なため、外部(application層)から直接呼ばれる
 * public メソッドに付与する必要がある(同一クラス内からの自己呼び出しではプロキシを経由せず
 * 無効化されてしまうため、{@link #callChatCompletions}という内部専用メソッドを分離している)。
 * </p>
 */
@Component
public class OpenAiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiClient.class);

    private final OpenAiProperties properties;
    private final WebClient webClient;

    public OpenAiClient(OpenAiProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getBaseUrl()).build();
    }

    /**
     * system/userプロンプトを送信し、JSON形式のモデル応答テキストを返す（{@code response_format=json_object}）。
     *
     * @throws ExternalApiException API呼び出しに失敗した場合
     */
    @Retry(name = "openai")
    @CircuitBreaker(name = "openai", fallbackMethod = "chatCompleteFallback")
    public String chatComplete(String systemPrompt, String userPrompt) {
        return callChatCompletions(systemPrompt, userPrompt, Map.of("type", "json_object"));
    }

    @SuppressWarnings("unused")
    private String chatCompleteFallback(String systemPrompt, String userPrompt, Throwable t) {
        return handleFallback("chatComplete", t);
    }

    /**
     * system/userプロンプトを送信し、自由記述の平文モデル応答テキストを返す（JSON形式を強制しない）。
     * 分析の説明文等、構造化不要な出力を得たい場合に使用する（例: 競合との差分説明、Phase9）。
     *
     * @throws ExternalApiException API呼び出しに失敗した場合
     */
    @Retry(name = "openai")
    @CircuitBreaker(name = "openai", fallbackMethod = "chatCompleteAsPlainTextFallback")
    public String chatCompleteAsPlainText(String systemPrompt, String userPrompt) {
        return callChatCompletions(systemPrompt, userPrompt, null);
    }

    @SuppressWarnings("unused")
    private String chatCompleteAsPlainTextFallback(String systemPrompt, String userPrompt, Throwable t) {
        return handleFallback("chatCompleteAsPlainText", t);
    }

    /** サーキットブレーカーOPEN時、またはリトライを使い切っても失敗した場合に呼ばれる。 */
    private String handleFallback(String methodName, Throwable t) {
        log.error("OpenAI API call ({}) failed after retries, or circuit breaker is open", methodName, t);
        throw new ExternalApiException("OpenAI API is currently unavailable. Please try again later.", t);
    }

    @SuppressWarnings("unchecked")
    private String callChatCompletions(String systemPrompt, String userPrompt, Map<String, String> responseFormat) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", properties.getModel());
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        ));
        requestBody.put("temperature", 0.7);
        if (responseFormat != null) {
            requestBody.put("response_format", responseFormat);
        }

        try {
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .block();

            if (response == null) {
                throw new ExternalApiException("OpenAI API returned an empty response");
            }
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> firstChoice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
            return (String) message.get("content");
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("OpenAI API call failed", e);
            throw new ExternalApiException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }
}
