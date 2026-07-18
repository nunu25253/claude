package com.buzzanalysis.domain.rankingscore.strategy;

import com.buzzanalysis.domain.rankingscore.RankingScoreInput;

/**
 * 既存の{@code BuzzScore}（エンゲージメント率・コメント率・AI分析・投稿構成・ハッシュタグを内包する
 * 統合スコア）をそのまま再利用する。二重計上を避けるため、これらの観点をPhase7側で個別に再実装しない
 * （docs/phases/phase7_ranking.md 参照）。
 */
public class BuzzScoreRankingStrategy implements RankingScoreStrategy {

    private static final double WEIGHT = 0.30;
    private static final double NEUTRAL_SCORE = 50.0;

    @Override
    public String name() {
        return "buzzScore";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(RankingScoreInput input) {
        return input.buzzScore() != null ? input.buzzScore().getTotalScore() : NEUTRAL_SCORE;
    }
}
