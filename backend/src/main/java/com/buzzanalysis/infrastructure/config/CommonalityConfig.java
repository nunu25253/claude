package com.buzzanalysis.infrastructure.config;

import com.buzzanalysis.domain.commonality.CommonalityStatisticsCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Phase8（共通点分析）のドメインサービス（フレームワーク非依存のPOJO）をSpring Beanとして組み立てる。
 * {@link PreprocessingConfig} と同じ方針で、ドメイン層自体はSpringに依存させず配線のみをinfrastructure層で行う。
 */
@Configuration
public class CommonalityConfig {

    @Bean
    public CommonalityStatisticsCalculator commonalityStatisticsCalculator() {
        return new CommonalityStatisticsCalculator();
    }
}
