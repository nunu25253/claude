package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.domain.common.exception.ExternalApiException;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * {@link EmbeddingClient} のOpenAI実装。{@code POST /v1/embeddings} を呼び出す。
 * APIキー未設定時、またはAPI呼び出しに失敗した場合は、テキストのハッシュ値をシードにした
 * <b>決定的な擬似ベクトル</b>にフォールバックする。擬似ベクトルは実際の意味的類似度を表さないため、
 * {@code model} フィールドを {@code "stub-deterministic-v1"} とし、実APIの出力と判別できるようにしている
 * （実測値と推定値/擬似値を混同しないという最重要ルールの精神を踏襲）。
 */
@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiEmbeddingClient.class);
    private static final String STUB_MODEL_NAME = "stub-deterministic-v1";

    private final OpenAiProperties properties;
    private final WebClient webClient;

    public OpenAiEmbeddingClient(OpenAiProperties properties, WebClient.Builder webClientBuilder) {
        this.properties = properties;
        this.webClient = webClientBuilder.baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public EmbeddingResult embed(String text) {
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using deterministic stub embedding");
            return stubEmbedding(text);
        }
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", properties.getEmbeddingModel(),
                    "input", text
            );
            Map<String, Object> response = webClient.post()
                    .uri("/embeddings")
                    .header("Authorization", "Bearer " + properties.getApiKey())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                    .block();

            if (response == null) {
                throw new ExternalApiException("OpenAI Embeddings API returned an empty response");
            }
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            List<Number> embeddingNumbers = (List<Number>) data.get(0).get("embedding");
            float[] vector = toFloatArray(embeddingNumbers);
            return new EmbeddingResult(vector, properties.getEmbeddingModel(), vector.length);
        } catch (Exception e) {
            log.warn("OpenAI Embeddings API call failed, falling back to stub embedding: {}", e.getMessage());
            return stubEmbedding(text);
        }
    }

    private float[] toFloatArray(List<Number> numbers) {
        float[] vector = new float[numbers.size()];
        for (int i = 0; i < numbers.size(); i++) {
            vector[i] = numbers.get(i).floatValue();
        }
        return vector;
    }

    /** テキストのSHA-256ハッシュをシードにした、同一入力に対して常に同じ結果を返す擬似ベクトル。 */
    private EmbeddingResult stubEmbedding(String text) {
        int dimensions = properties.getEmbeddingDimensions();
        long seed = hashToSeed(text == null ? "" : text);
        Random random = new Random(seed);
        float[] vector = new float[dimensions];
        double sumOfSquares = 0.0;
        for (int i = 0; i < dimensions; i++) {
            float value = (float) random.nextGaussian();
            vector[i] = value;
            sumOfSquares += value * value;
        }
        normalize(vector, sumOfSquares);
        return new EmbeddingResult(vector, STUB_MODEL_NAME, dimensions);
    }

    private void normalize(float[] vector, double sumOfSquares) {
        double norm = Math.sqrt(sumOfSquares);
        if (norm == 0.0) {
            return;
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / norm);
        }
    }

    private long hashToSeed(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            long seed = 0L;
            for (int i = 0; i < 8; i++) {
                seed = (seed << 8) | (hash[i] & 0xFF);
            }
            return seed;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
