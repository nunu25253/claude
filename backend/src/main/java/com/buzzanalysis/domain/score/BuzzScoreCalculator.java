package com.buzzanalysis.domain.score;

import com.buzzanalysis.domain.score.strategy.BuzzScoreStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * バズスコア算出のContext（Strategyパターン）。注入された全{@link BuzzScoreStrategy}を実行し、
 * 各スコアの重み付き合計として0〜100のバズスコアを算出する。
 * フレームワーク非依存の純粋なドメインサービス。Strategyのリストはコンストラクタインジェクションで受け取る。
 */
public class BuzzScoreCalculator {

    private final List<BuzzScoreStrategy> strategies;

    public BuzzScoreCalculator(List<BuzzScoreStrategy> strategies) {
        this.strategies = Objects.requireNonNull(strategies, "strategies must not be null");
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("at least one BuzzScoreStrategy is required");
        }
    }

    /**
     * 各Strategyのスコアと重み付き合計スコアを算出する。
     *
     * @return breakdown（Strategy名 -> スコア）と合計スコアを含む算出結果
     */
    public CalculationResult calculate(BuzzScoreInput input) {
        Map<String, Double> breakdown = new LinkedHashMap<>();
        double totalWeight = 0.0;
        double weightedSum = 0.0;

        for (BuzzScoreStrategy strategy : strategies) {
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

    /** 算出結果（合計スコアとStrategyごとの内訳）を保持する値オブジェクト。 */
    public record CalculationResult(double totalScore, Map<String, Double> breakdown) {
    }
}
