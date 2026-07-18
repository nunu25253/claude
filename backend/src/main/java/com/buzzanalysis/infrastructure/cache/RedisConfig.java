package com.buzzanalysis.infrastructure.cache;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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

    // アプリ共通のObjectMapperをそのまま渡すと、キャッシュ対象がList<record>等の場合に型情報が
    // Redis上のJSONへ書き込まれず、キャッシュヒット時の読み戻しでrecordの各要素がLinkedHashMapに
    // なってしまい再シリアライズに失敗する(HttpMessageNotWritableException)。Redis専用にコピーした
    // ObjectMapperへ多態的型情報を有効化することで、キャッシュ対象がrecord(finalクラス)であっても
    // 型情報を保持できるようにする(EVERYTHINGはfinalクラスも対象にする点がNON_FINALと異なる)。
    // DefaultTyping.EVERYTHINGは非推奨だが、個別のDTOに@JsonTypeInfoを注釈せずに済む実用的な
    // 回避策として意図的に使用する。
    @SuppressWarnings("deprecation")
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        ObjectMapper redisObjectMapper = objectMapper.copy();
        redisObjectMapper.activateDefaultTyping(
                redisObjectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY);
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

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
