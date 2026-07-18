package com.buzzanalysis.domain.embedding;

/**
 * Embedding生成APIを抽象化するドメインインターフェース。OpenAI Embeddings APIの実装は
 * infrastructure層に置く（{@code OpenAiEmbeddingClient}）。将来他のEmbeddingプロバイダに
 * 差し替える場合も、このインターフェースを実装するだけでよい。
 */
public interface EmbeddingClient {

    /** 指定テキストのEmbeddingベクトルを生成する。 */
    EmbeddingResult embed(String text);
}
