package com.buzzanalysis.application.embedding.dto;

import com.buzzanalysis.domain.embedding.EmbeddingTarget;

import java.util.List;
import java.util.UUID;

/**
 * Embedding生成1回分の実行結果サマリー。
 *
 * @param postId    対象の投稿ID
 * @param generated 新規生成/再生成した対象一覧
 * @param skipped   本文が前回生成時から変更なしのためAPI呼び出しをスキップした対象一覧（コスト削減）
 * @param embeddings 生成後の最新Embedding一覧
 */
public record EmbeddingGenerationSummaryDto(
        UUID postId,
        List<EmbeddingTarget> generated,
        List<EmbeddingTarget> skipped,
        List<EmbeddingDto> embeddings
) {
}
