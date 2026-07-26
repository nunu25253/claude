package com.buzzanalysis.infrastructure.persistence.repository;

import java.util.UUID;

/**
 * {@link EmbeddingJpaRepository#findNearest} が返すネイティブクエリ結果のインターフェース射影。
 * SQL側の列エイリアス（{@code postId}, {@code similarity}）とゲッター名が一致している必要がある。
 */
public interface EmbeddingSimilarityRow {

    UUID getPostId();

    double getSimilarity();
}
