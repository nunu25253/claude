package com.buzzanalysis.domain.matching;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;

/**
 * 一致率計算に必要な入力をまとめた値オブジェクト。
 *
 * @param post                対象投稿
 * @param analysisResult      AI分析結果（Phase5）。未分析の投稿はnull
 * @param preprocessedPost    前処理結果（Phase2）
 * @param condition           ユーザー検索条件
 * @param semanticSimilarity  Phase4のベクトル検索で得られたコサイン類似度。意味検索を経由しない場合はnull
 */
public record MatchRateInput(
        Post post,
        AnalysisResult analysisResult,
        PreprocessedPost preprocessedPost,
        UserSearchCondition condition,
        Double semanticSimilarity
) {
}
