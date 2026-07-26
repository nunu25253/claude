package com.buzzanalysis.domain.rankingscore.strategy;

import com.buzzanalysis.domain.rankingscore.RankingScoreInput;

/** Phase6で算出したユーザー条件との一致率をそのまま採用する。文脈が無い場合は中立スコア。 */
public class MatchRateRankingStrategy implements RankingScoreStrategy {

    private static final double WEIGHT = 0.30;
    private static final double NEUTRAL_SCORE = 50.0;

    @Override
    public String name() {
        return "matchRate";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(RankingScoreInput input) {
        return input.matchRatePercent() != null ? input.matchRatePercent() : NEUTRAL_SCORE;
    }
}
