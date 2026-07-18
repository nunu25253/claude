package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.matching.MatchRateInput;
import com.buzzanalysis.domain.preprocessing.ContentFormat;

/** 投稿形式（Phase2の{@link ContentFormat}）の一致度に基づくスコア。 */
public class PostFormatMatchStrategy implements MatchRateStrategy {

    private static final double WEIGHT = 0.10;
    private static final double NEUTRAL_SCORE = 50.0;

    @Override
    public String name() {
        return "postFormat";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(MatchRateInput input) {
        ContentFormat requested = input.condition().getPostFormat();
        if (requested == null) {
            return NEUTRAL_SCORE;
        }
        ContentFormat actual = input.preprocessedPost() == null ? null : input.preprocessedPost().contentFormat();
        return requested.equals(actual) ? 100.0 : 15.0;
    }
}
