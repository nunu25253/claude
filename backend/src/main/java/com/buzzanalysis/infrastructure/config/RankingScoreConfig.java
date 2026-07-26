package com.buzzanalysis.infrastructure.config;

import com.buzzanalysis.domain.rankingscore.RankingScoreCalculator;
import com.buzzanalysis.domain.rankingscore.strategy.BuzzScoreRankingStrategy;
import com.buzzanalysis.domain.rankingscore.strategy.FreshnessRankingStrategy;
import com.buzzanalysis.domain.rankingscore.strategy.LikeRateRankingStrategy;
import com.buzzanalysis.domain.rankingscore.strategy.MatchRateRankingStrategy;
import com.buzzanalysis.domain.rankingscore.strategy.RankingScoreStrategy;
import com.buzzanalysis.domain.rankingscore.strategy.VideoDurationRankingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Phase7（ランキングAI）のドメインStrategy群（フレームワーク非依存のPOJO）をSpring Beanとして組み立てる。
 * {@link BuzzScoreConfig} と同じ方針。
 */
@Configuration
public class RankingScoreConfig {

    @Bean
    public List<RankingScoreStrategy> rankingScoreStrategies() {
        return List.of(
                new MatchRateRankingStrategy(),
                new BuzzScoreRankingStrategy(),
                new LikeRateRankingStrategy(),
                new FreshnessRankingStrategy(),
                new VideoDurationRankingStrategy()
        );
    }

    @Bean
    public RankingScoreCalculator rankingScoreCalculator(List<RankingScoreStrategy> rankingScoreStrategies) {
        return new RankingScoreCalculator(rankingScoreStrategies);
    }
}
