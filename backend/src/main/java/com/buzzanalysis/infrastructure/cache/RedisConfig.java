package com.buzzanalysis.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

/**
 * Redisキャッシュ設定（Spring Data Redis）。競合統計/ランキングの算出結果をキャッシュし、
 * {@link com.buzzanalysis.application.event.AnalysisCompletedEventListener}（Observer）が分析完了時に無効化する。
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(15))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        Map<String, RedisCacheConfiguration> perCacheConfig = Map.of(
                "competitorStats", defaultConfig.entryTtl(Duration.ofMinutes(30)),
                "rankings", defaultConfig.entryTtl(Duration.ofMinutes(10)),
                // 意味検索結果(Phase4)。同一キーワードの再検索でOpenAI Embeddings API呼び出しと
                // pgvector検索の両方を削減する。新規Embedding生成時の明示的なキャッシュ無効化は
                // 未実装のため、TTLを短めにしている(docs/phases/phase4_semantic_search.md参照)。
                "semanticSearchResults", defaultConfig.entryTtl(Duration.ofMinutes(10))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(perCacheConfig)
                .build();
    }
}
