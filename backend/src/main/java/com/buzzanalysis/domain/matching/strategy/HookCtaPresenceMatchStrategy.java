package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.matching.MatchRateInput;

/**
 * フック・CTAが明確に分析されているかに基づくスコア（要求仕様の「構成・CTA・フックなどを総合評価する」に対応）。
 * ユーザー条件の有無によらず、常に評価対象とする（お手本となる投稿ほどフック/CTAが明確であるため）。
 */
public class HookCtaPresenceMatchStrategy implements MatchRateStrategy {

    private static final double WEIGHT = 0.15;
    private static final int SUBSTANTIVE_LENGTH_THRESHOLD = 5;

    @Override
    public String name() {
        return "hookCtaPresence";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(MatchRateInput input) {
        AnalysisResult result = input.analysisResult();
        if (result == null) {
            return 30.0;
        }
        boolean hasHook = isSubstantive(result.getHook());
        boolean hasCta = isSubstantive(result.getCallToAction());
        if (hasHook && hasCta) {
            return 100.0;
        }
        if (hasHook || hasCta) {
            return 55.0;
        }
        return 10.0;
    }

    private boolean isSubstantive(String value) {
        return value != null && value.trim().length() >= SUBSTANTIVE_LENGTH_THRESHOLD;
    }
}
