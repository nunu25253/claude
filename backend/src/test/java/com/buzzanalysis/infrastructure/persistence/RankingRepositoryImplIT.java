package com.buzzanalysis.infrastructure.persistence;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.ranking.Ranking;
import com.buzzanalysis.domain.ranking.RankingType;
import com.buzzanalysis.infrastructure.persistence.adapter.RankingRepositoryImpl;
import com.buzzanalysis.infrastructure.persistence.mapper.PlatformMapper;
import com.buzzanalysis.infrastructure.persistence.mapper.RankingMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link RankingRepositoryImpl} のTestcontainers(PostgreSQL)を使った統合テスト。
 * genre/platform未指定時に「全体版(該当カラムがnullの行)のみ」が返り、ジャンル別/プラットフォーム別の行が
 * 混在しない(=同一投稿の重複が起きない)ことを検証する。
 */
@Testcontainers
@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({RankingMapper.class, PlatformMapper.class, RankingRepositoryImpl.class})
class RankingRepositoryImplIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("buzz_analysis_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private RankingRepositoryImpl rankingRepository;

    @Test
    void findByFilters_withNoGenreOrPlatform_returnsOnlyTheOverallRows_notGenreOrPlatformSpecificOnes() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(1);
        OffsetDateTime end = OffsetDateTime.now();
        UUID overallPostId = UUID.randomUUID();
        UUID genrePostId = UUID.randomUUID();
        UUID platformPostId = UUID.randomUUID();

        rankingRepository.save(List.of(
                Ranking.createNew(RankingType.TRENDING, null, null, overallPostId, 1, 90.0, start, end),
                Ranking.createNew(RankingType.TRENDING, "TECH", null, genrePostId, 1, 80.0, start, end),
                Ranking.createNew(RankingType.TRENDING, null, Platform.INSTAGRAM, platformPostId, 1, 70.0, start, end)
        ));

        List<Ranking> result = rankingRepository.findByFilters(RankingType.TRENDING, null, null, 20);

        assertThat(result).extracting(Ranking::getPostId).containsExactly(overallPostId);
    }

    @Test
    void findByFilters_withGenre_returnsOnlyMatchingGenreRows() {
        OffsetDateTime start = OffsetDateTime.now().minusDays(1);
        OffsetDateTime end = OffsetDateTime.now();
        UUID techPostId = UUID.randomUUID();
        UUID beautyPostId = UUID.randomUUID();

        rankingRepository.save(List.of(
                Ranking.createNew(RankingType.TRENDING, "TECH", null, techPostId, 1, 80.0, start, end),
                Ranking.createNew(RankingType.TRENDING, "BEAUTY", null, beautyPostId, 1, 75.0, start, end)
        ));

        List<Ranking> result = rankingRepository.findByFilters(RankingType.TRENDING, "TECH", null, 20);

        assertThat(result).extracting(Ranking::getPostId).containsExactly(techPostId);
    }
}
