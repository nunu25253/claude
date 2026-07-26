package com.buzzanalysis.infrastructure.config;

import com.buzzanalysis.domain.normalization.DefaultPostNormalizer;
import com.buzzanalysis.domain.normalization.PostFieldMapper;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.normalization.strategy.DefaultNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.InstagramNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.PlatformNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.TikTokNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.XNormalizationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Phase1（正規化レイヤー）のドメインStrategy群（フレームワーク非依存のPOJO）をSpring Beanとして組み立てる。
 * {@link BuzzScoreConfig} と同じ方針で、ドメイン層自体はSpringに依存させず配線のみをinfrastructure層で行う。
 * 汎用フォールバック（{@link DefaultNormalizationStrategy}）は必ずリストの末尾に置くこと。
 */
@Configuration
public class NormalizationConfig {

    @Bean
    public List<PlatformNormalizationStrategy> platformNormalizationStrategies() {
        return List.of(
                new InstagramNormalizationStrategy(),
                new TikTokNormalizationStrategy(),
                new XNormalizationStrategy(),
                new DefaultNormalizationStrategy()
        );
    }

    @Bean
    public PostFieldMapper postFieldMapper() {
        return new PostFieldMapper();
    }

    @Bean
    public PostNormalizer postNormalizer(PostFieldMapper postFieldMapper,
                                          List<PlatformNormalizationStrategy> platformNormalizationStrategies) {
        return new DefaultPostNormalizer(postFieldMapper, platformNormalizationStrategies);
    }
}
