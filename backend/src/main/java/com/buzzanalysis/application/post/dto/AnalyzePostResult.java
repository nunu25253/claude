package com.buzzanalysis.application.post.dto;

import java.util.List;

/**
 * 投稿URL分析ユースケースの出力結果。投稿データ、AI分析、バズスコア、類似投稿提案をまとめて返す。
 */
public record AnalyzePostResult(
        PostDto post,
        AnalysisResultDto analysis,
        BuzzScoreDto buzzScore,
        List<PostDto> similarPosts
) {
}
