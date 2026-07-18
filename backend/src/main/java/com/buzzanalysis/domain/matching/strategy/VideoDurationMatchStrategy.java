package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.matching.MatchRateInput;
import com.buzzanalysis.domain.preprocessing.VideoDurationInfo;

/** 希望動画時間（秒）と実際の動画時間との近さに基づくスコア。 */
public class VideoDurationMatchStrategy implements MatchRateStrategy {

    private static final double WEIGHT = 0.10;
    private static final double NEUTRAL_SCORE = 50.0;

    @Override
    public String name() {
        return "videoDuration";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(MatchRateInput input) {
        Integer requested = input.condition().getVideoDurationSeconds();
        if (requested == null || requested <= 0) {
            return NEUTRAL_SCORE;
        }
        VideoDurationInfo actual = input.preprocessedPost() == null ? null : input.preprocessedPost().videoDuration();
        if (actual == null || actual.durationSeconds() == null) {
            return 10.0; // 動画を含まない投稿は希望動画時間の条件と噛み合わない
        }
        double diffRatio = Math.abs(actual.durationSeconds() - requested) / (double) requested;
        if (diffRatio <= 0.2) {
            return 100.0;
        }
        if (diffRatio <= 0.5) {
            return 60.0;
        }
        return 20.0;
    }
}
