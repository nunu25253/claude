package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.domain.common.exception.ExternalApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * OpenAI Chat Completions APIをWebClientで直接呼び出す薄いクライアント（SDK不使用）。
 * {@code POST /v1/chat/completions} を呼び出し、応答メッセージの文字列を返す。
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
     * system/userプロンプトを送信し、モデルの応答テキストを返す。
     *
     * @throws ExternalApiException API呼び出しに失敗した場合
     */
    @SuppressWarnings("unchecked")
    public String chatComplete(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = Map.of(
                "model", properties.getModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.7,
                "response_format", Map.of("type", "json_object")
        );

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
