package com.buzzanalysis.domain.embedding;

/**
 * {@link EmbeddingClient} が返すEmbedding生成結果。
 *
 * @param vector     生成されたベクトル
 * @param model      使用したモデル名（APIキー未設定時のフォールバックは "stub-deterministic-v1" 等、実API利用と判別可能な値にする）
 * @param dimensions ベクトルの次元数
 */
public record EmbeddingResult(float[] vector, String model, int dimensions) {
}
