package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.matching.MatchRateInput;

import java.util.Locale;

/**
 * ターゲット年齢/性別と、AIが生成した自由文の{@code targetAudience}とのキーワード包含判定（ヒューリスティック）。
 * {@code targetAudience}は構造化されていないため、表記揺れには対応できない既知の制約がある
 * （docs/phases/phase6_condition_matching.md 参照）。
 */
public class TargetAudienceMatchStrategy implements MatchRateStrategy {

    private static final double WEIGHT = 0.10;
    private static final double NEUTRAL_SCORE = 50.0;
    private static final double NOT_ANALYZED_SCORE = 30.0;

    @Override
    public String name() {
        return "targetAudience";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(MatchRateInput input) {
        String ageRange = input.condition().getTargetAgeRange();
        String gender = input.condition().getTargetGender();
        boolean ageSpecified = !isBlank(ageRange);
        boolean genderSpecified = !isBlank(gender);
        if (!ageSpecified && !genderSpecified) {
            return NEUTRAL_SCORE;
        }
        AnalysisResult result = input.analysisResult();
        if (result == null || isBlank(result.getTargetAudience())) {
            return NOT_ANALYZED_SCORE;
        }

        String text = result.getTargetAudience().toLowerCase(Locale.ROOT);
        int specifiedCount = (ageSpecified ? 1 : 0) + (genderSpecified ? 1 : 0);
        int matchedCount = 0;
        if (ageSpecified && text.contains(ageRange.toLowerCase(Locale.ROOT))) {
            matchedCount++;
        }
        if (genderSpecified && text.contains(gender.toLowerCase(Locale.ROOT))) {
            matchedCount++;
        }
        return (matchedCount / (double) specifiedCount) * 100.0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
