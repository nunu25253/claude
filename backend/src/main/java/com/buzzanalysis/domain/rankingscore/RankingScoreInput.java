package com.buzzanalysis.domain.rankingscore;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.domain.score.BuzzScore;

/**
 * ランキングスコア算出に必要な入力（Phase7）。
 *
 * @param post              対象投稿
 * @param analysisResult    AI分析結果（Phase5）。未分析ならnull
 * @param preprocessedPost  前処理結果（Phase2）
 * @param buzzScore         既存のバズスコア。未算出ならnull
 * @param matchRatePercent  Phase6で算出したユーザー条件との一致率(0〜100)。文脈が無い場合はnull
 */
public record RankingScoreInput(
        Post post,
        AnalysisResult analysisResult,
        PreprocessedPost preprocessedPost,
        BuzzScore buzzScore,
        Double matchRatePercent
) {
}
