package com.buzzanalysis.domain.rankingscore.strategy;

import com.buzzanalysis.domain.rankingscore.RankingScoreInput;

/** ランキングスコア算出のためのStrategyインターフェース（Phase7、{@code BuzzScoreStrategy}と同型）。 */
public interface RankingScoreStrategy {

    String name();

    double weight();

    double score(RankingScoreInput input);
}
