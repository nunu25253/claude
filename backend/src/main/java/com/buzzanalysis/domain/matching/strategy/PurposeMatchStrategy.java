package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.matching.MatchRateInput;

import java.util.Locale;

/**
 * 投稿目的（アフィリエイト目的/CV目的/保存目的/再生数目的等、ユーザーが自由文で指定）と、
 * Phase5で分析済みの{@code postPurpose}とのキーワード包含判定。
 */
public class PurposeMatchStrategy implements MatchRateStrategy {

    private static final double WEIGHT = 0.10;
    private static final double NEUTRAL_SCORE = 50.0;
    private static final double NOT_ANALYZED_SCORE = 30.0;

    @Override
    public String name() {
        return "purpose";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(MatchRateInput input) {
        String requestedPurpose = input.condition().getPurpose();
        if (requestedPurpose == null || requestedPurpose.isBlank()) {
            return NEUTRAL_SCORE;
        }
        AnalysisResult result = input.analysisResult();
        if (result == null || result.getPostPurpose() == null || result.getPostPurpose().isBlank()) {
            return NOT_ANALYZED_SCORE;
        }
        boolean matches = result.getPostPurpose().toLowerCase(Locale.ROOT)
                .contains(requestedPurpose.toLowerCase(Locale.ROOT));
        return matches ? 100.0 : 25.0;
    }
}
