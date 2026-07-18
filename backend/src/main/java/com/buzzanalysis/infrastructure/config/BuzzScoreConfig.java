package com.buzzanalysis.infrastructure.config;

import com.buzzanalysis.domain.score.BuzzScoreCalculator;
import com.buzzanalysis.domain.score.strategy.AiAnalysisStrategy;
import com.buzzanalysis.domain.score.strategy.BuzzScoreStrategy;
import com.buzzanalysis.domain.score.strategy.CommentRateStrategy;
import com.buzzanalysis.domain.score.strategy.ContentStructureStrategy;
import com.buzzanalysis.domain.score.strategy.EngagementRateStrategy;
import com.buzzanalysis.domain.score.strategy.HashtagStrategy;
import com.buzzanalysis.domain.score.strategy.PostFormatStrategy;
import com.buzzanalysis.domain.score.strategy.PostTimingStrategy;
import com.buzzanalysis.domain.score.strategy.ViewCountStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * ドメイン層のStrategy実装（フレームワーク非依存のPOJO）をSpring Beanとして組み立てる設定クラス。
 * ドメイン層自体はSpringに依存させず、DIの配線のみをinfrastructure層で行う（Clean Architectureの原則）。
 */
@Configuration
public class BuzzScoreConfig {

    @Bean
    public List<BuzzScoreStrategy> buzzScoreStrategies() {
        return List.of(
                new EngagementRateStrategy(),
                new ViewCountStrategy(),
                new CommentRateStrategy(),
                new PostFormatStrategy(),
                new HashtagStrategy(),
                new PostTimingStrategy(),
                new ContentStructureStrategy(),
                new AiAnalysisStrategy()
        );
    }

    @Bean
    public BuzzScoreCalculator buzzScoreCalculator(List<BuzzScoreStrategy> buzzScoreStrategies) {
        return new BuzzScoreCalculator(buzzScoreStrategies);
    }
}
