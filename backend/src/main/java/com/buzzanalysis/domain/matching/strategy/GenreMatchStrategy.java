package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.matching.MatchRateInput;

import java.util.Locale;

/** ジャンル/サブジャンルの一致度（Phase5 {@code AnalysisResult}）に基づくスコア。 */
public class GenreMatchStrategy implements MatchRateStrategy {

    private static final double WEIGHT = 0.15;
    private static final double NEUTRAL_SCORE = 50.0;
    private static final double NOT_ANALYZED_SCORE = 30.0;

    @Override
    public String name() {
        return "genre";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(MatchRateInput input) {
        String conditionGenre = input.condition().getGenre();
        String conditionSubGenre = input.condition().getSubGenre();
        if (isBlank(conditionGenre) && isBlank(conditionSubGenre)) {
            return NEUTRAL_SCORE;
        }
        AnalysisResult result = input.analysisResult();
        if (result == null) {
            return NOT_ANALYZED_SCORE;
        }

        boolean genreMatches = !isBlank(conditionGenre) && contains(result.getGenre(), conditionGenre);
        boolean subGenreMatches = !isBlank(conditionSubGenre) && contains(result.getSubGenre(), conditionSubGenre);

        int specifiedCount = (isBlank(conditionGenre) ? 0 : 1) + (isBlank(conditionSubGenre) ? 0 : 1);
        int matchedCount = (genreMatches ? 1 : 0) + (subGenreMatches ? 1 : 0);
        if (specifiedCount == 0) {
            return NEUTRAL_SCORE;
        }
        return (matchedCount / (double) specifiedCount) * 100.0;
    }

    private boolean contains(String haystack, String needle) {
        if (isBlank(haystack)) {
            return false;
        }
        return haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
