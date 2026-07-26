package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.score.BuzzScoreInput;

/**
 * バズスコア算出のためのStrategyインターフェース。各実装は独立した観点（エンゲージメント率、再生数、
 * 投稿フォーマット等）から0.0〜100.0のスコアを算出する。{@code BuzzScoreCalculator}（Context）が
 * 全Strategyを重み付き合計して最終スコアを求める。
 */
public interface BuzzScoreStrategy {

    /**
     * このStrategyが表す観点の名前（内訳のキーとして使用）。
     */
    String name();

    /**
     * 最終スコアに対するこのStrategyの重み（0.0〜1.0）。全Strategyの重みの合計が1.0になるよう設計する。
     */
    double weight();

    /**
     * 0.0〜100.0の範囲でこの観点のスコアを算出する。
     */
    double score(BuzzScoreInput input);
}
