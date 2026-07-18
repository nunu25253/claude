package com.buzzanalysis.infrastructure.config;

import com.buzzanalysis.domain.genre.GenreNormalizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ジャンル正規化ドメインサービス（フレームワーク非依存のPOJO）をSpring Beanとして組み立てる。
 * {@link CommonalityConfig} と同じ方針で、ドメイン層自体はSpringに依存させず配線のみをinfrastructure層で行う。
 */
@Configuration
public class GenreConfig {

    @Bean
    public GenreNormalizer genreNormalizer() {
        return new GenreNormalizer();
    }
}
