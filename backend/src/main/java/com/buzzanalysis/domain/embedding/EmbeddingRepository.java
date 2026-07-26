package com.buzzanalysis.domain.embedding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Embedding集約のリポジトリインターフェース。 */
public interface EmbeddingRepository {

    Embedding save(Embedding embedding);

    Optional<Embedding> findByPostIdAndTarget(UUID postId, EmbeddingTarget target);

    List<Embedding> findAllByPostId(UUID postId);

    /**
     * 指定ターゲットのEmbeddingの中から、クエリベクトルにコサイン類似度が近い順に上位 {@code limit} 件を返す
     * （Phase4: 意味検索エンジン）。Embeddingが1件も生成されていない投稿は結果に含まれない。
     */
    List<SimilarityMatch> findNearest(EmbeddingTarget target, float[] queryVector, int limit);
}
