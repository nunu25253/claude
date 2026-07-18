package com.buzzanalysis.domain.rankingscore.strategy;

import com.buzzanalysis.domain.preprocessing.VideoDurationInfo;
import com.buzzanalysis.domain.rankingscore.RankingScoreInput;

/**
 * 動画時間が一般的に「視聴維持率が高いとされる」最適レンジ(15〜60秒)に近いかに基づくスコア（新規、Phase7）。
 * 動画を含まない投稿は対象外として中立スコアを返す（不当に減点しない）。最適レンジは一般論に基づく
 * ヒューリスティックであり、ジャンル/プラットフォームごとの最適値の違いは考慮していない
 * （docs/phases/phase7_ranking.md 参照）。
 */
public class VideoDurationRankingStrategy implements RankingScoreStrategy {

    private static final double WEIGHT = 0.10;
    private static final double NEUTRAL_SCORE = 50.0;
    private static final int OPTIMAL_MIN_SECONDS = 15;
    private static final int OPTIMAL_MAX_SECONDS = 60;

    @Override
    public String name() {
        return "videoDuration";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(RankingScoreInput input) {
        VideoDurationInfo info = input.preprocessedPost() == null ? null : input.preprocessedPost().videoDuration();
        if (info == null || info.durationSeconds() == null) {
            return NEUTRAL_SCORE;
        }
        int seconds = info.durationSeconds();
        if (seconds >= OPTIMAL_MIN_SECONDS && seconds <= OPTIMAL_MAX_SECONDS) {
            return 100.0;
        }
        int distance = seconds < OPTIMAL_MIN_SECONDS ? OPTIMAL_MIN_SECONDS - seconds : seconds - OPTIMAL_MAX_SECONDS;
        return Math.max(20.0, 100.0 - distance * 2.0);
    }
}
