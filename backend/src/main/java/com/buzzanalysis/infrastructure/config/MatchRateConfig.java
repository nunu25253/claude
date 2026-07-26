package com.buzzanalysis.infrastructure.config;

import com.buzzanalysis.domain.matching.MatchRateCalculator;
import com.buzzanalysis.domain.matching.strategy.GenreMatchStrategy;
import com.buzzanalysis.domain.matching.strategy.HookCtaPresenceMatchStrategy;
import com.buzzanalysis.domain.matching.strategy.MatchRateStrategy;
import com.buzzanalysis.domain.matching.strategy.PostFormatMatchStrategy;
import com.buzzanalysis.domain.matching.strategy.PurposeMatchStrategy;
import com.buzzanalysis.domain.matching.strategy.SemanticSimilarityMatchStrategy;
import com.buzzanalysis.domain.matching.strategy.TargetAudienceMatchStrategy;
import com.buzzanalysis.domain.matching.strategy.VideoDurationMatchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Phase6（ユーザー条件分析）のドメインStrategy群（フレームワーク非依存のPOJO）をSpring Beanとして組み立てる。
 * {@link BuzzScoreConfig} と同じ方針。
 */
@Configuration
public class MatchRateConfig {

    @Bean
    public List<MatchRateStrategy> matchRateStrategies() {
        return List.of(
                new SemanticSimilarityMatchStrategy(),
                new GenreMatchStrategy(),
                new HookCtaPresenceMatchStrategy(),
                new TargetAudienceMatchStrategy(),
                new PostFormatMatchStrategy(),
                new VideoDurationMatchStrategy(),
                new PurposeMatchStrategy()
        );
    }

    @Bean
    public MatchRateCalculator matchRateCalculator(List<MatchRateStrategy> matchRateStrategies) {
        return new MatchRateCalculator(matchRateStrategies);
    }
}
