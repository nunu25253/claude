package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.matching.MatchRateInput;

/**
 * 一致率算出のためのStrategyインターフェース（{@code BuzzScoreStrategy}と同型）。各実装は独立した観点
 * （意味的類似度、ジャンル一致、ターゲット一致等）から0.0〜100.0のスコアを算出する。
 * 条件が未指定の観点は中立スコア（50.0）を返し、算出結果を不当に引き下げない。
 */
public interface MatchRateStrategy {

    String name();

    double weight();

    double score(MatchRateInput input);
}
