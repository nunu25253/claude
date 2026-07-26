package com.buzzanalysis.domain.rag;

import java.util.UUID;

/**
 * RAGベクトル類似検索の1件分の結果。
 *
 * @param documentId マッチしたドキュメントのID
 * @param similarity コサイン類似度（1.0に近いほど類似）
 */
public record RagSimilarityMatch(UUID documentId, double similarity) {
}
