package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.score.BuzzScoreInput;

/**
 * 再生数の絶対値に基づくスコア算出。桁数が大きく分布が偏るため対数スケールで評価する。
 */
public class ViewCountStrategy implements BuzzScoreStrategy {

    private static final double WEIGHT = 0.15;
    /** この再生数で満点(100点)とみなす基準値。 */
    private static final double SATURATION_VIEWS = 5_000_000.0;

    @Override
    public String name() {
        return "viewCount";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(BuzzScoreInput input) {
        Long views = input.post().getViewCount();
        if (views == null || views <= 0) {
            return 0.0;
        }
        double logScore = Math.log10(views + 1) / Math.log10(SATURATION_VIEWS + 1) * 100.0;
        return Math.max(0.0, Math.min(100.0, logScore));
    }
}
