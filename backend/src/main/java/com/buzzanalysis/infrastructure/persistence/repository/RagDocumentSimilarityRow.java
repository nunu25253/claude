package com.buzzanalysis.infrastructure.persistence.repository;

import java.util.UUID;

/**
 * {@link RagDocumentJpaRepository#findNearest} が返すネイティブクエリ結果のインターフェース射影。
 * SQL側の列エイリアス（{@code documentId}, {@code similarity}）とゲッター名が一致している必要がある。
 */
public interface RagDocumentSimilarityRow {

    UUID getDocumentId();

    double getSimilarity();
}
