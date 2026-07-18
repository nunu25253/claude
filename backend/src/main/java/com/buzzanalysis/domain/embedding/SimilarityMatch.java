package com.buzzanalysis.domain.embedding;

import java.util.UUID;

/**
 * ベクトル類似検索の1件分の結果。
 *
 * @param postId     マッチした投稿のID
 * @param similarity コサイン類似度（1.0に近いほど類似、pgvectorのコサイン距離から {@code 1 - distance} で算出）
 */
public record SimilarityMatch(UUID postId, double similarity) {
}
