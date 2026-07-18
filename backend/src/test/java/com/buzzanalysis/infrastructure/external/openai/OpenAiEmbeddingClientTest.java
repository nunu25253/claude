package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.domain.embedding.EmbeddingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link OpenAiEmbeddingClient} の単体テスト。APIキー未設定時の擬似ベクトルフォールバックを検証する
 * （実際のOpenAI API呼び出しは行わない。呼び出しの検証はAPIキーが未設定のため到達しない）。
 */
class OpenAiEmbeddingClientTest {

    private OpenAiProperties properties;
    private OpenAiEmbeddingClient client;

    @BeforeEach
    void setUp() {
        properties = new OpenAiProperties();
        properties.setApiKey(""); // 未設定 -> スタブにフォールバック
        client = new OpenAiEmbeddingClient(properties, WebClient.builder());
    }

    @Test
    void embed_returnsStubEmbedding_withConfiguredDimensions_whenApiKeyNotConfigured() {
        EmbeddingResult result = client.embed("テスト投稿の本文です");

        assertThat(result.model()).isEqualTo("stub-deterministic-v1");
        assertThat(result.dimensions()).isEqualTo(properties.getEmbeddingDimensions());
        assertThat(result.vector()).hasSize(properties.getEmbeddingDimensions());
    }

    @Test
    void embed_isDeterministic_forSameInputText() {
        EmbeddingResult first = client.embed("同じ入力テキスト");
        EmbeddingResult second = client.embed("同じ入力テキスト");

        assertThat(first.vector()).containsExactly(second.vector());
    }

    @Test
    void embed_producesDifferentVectors_forDifferentInputText() {
        EmbeddingResult a = client.embed("投稿A");
        EmbeddingResult b = client.embed("投稿B");

        assertThat(a.vector()).isNotEqualTo(b.vector());
    }
}
