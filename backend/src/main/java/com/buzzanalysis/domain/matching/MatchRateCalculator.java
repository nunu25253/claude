package com.buzzanalysis.domain.matching;

import com.buzzanalysis.domain.matching.strategy.MatchRateStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 一致率算出のContext（Strategyパターン、{@code BuzzScoreCalculator}と同型）。
 * 注入された全{@link MatchRateStrategy}を実行し、重み付き合計として0〜100の一致率を算出する。
 */
public class MatchRateCalculator {

    private final List<MatchRateStrategy> strategies;

    public MatchRateCalculator(List<MatchRateStrategy> strategies) {
        this.strategies = Objects.requireNonNull(strategies, "strategies must not be null");
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("at least one MatchRateStrategy is required");
        }
    }

    public CalculationResult calculate(MatchRateInput input) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (MatchRateStrategy strategy : strategies) {
            double rawScore = strategy.score(input);
            double clamped = Math.max(0.0, Math.min(100.0, rawScore));
            breakdown.put(strategy.name(), clamped);
            weightedSum += clamped * strategy.weight();
            totalWeight += strategy.weight();
        }

        double total = totalWeight > 0 ? weightedSum / totalWeight : 0.0;
        double finalScore = Math.round(total * 100.0) / 100.0;
        return new CalculationResult(finalScore, breakdown);
    }

    public record CalculationResult(double matchRatePercent, Map<String, Double> breakdown) {
    }
}
