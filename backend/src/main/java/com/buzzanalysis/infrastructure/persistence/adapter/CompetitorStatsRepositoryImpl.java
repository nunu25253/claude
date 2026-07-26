package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.competitor.CompetitorStats;
import com.buzzanalysis.domain.competitor.CompetitorStatsRepository;
import com.buzzanalysis.infrastructure.persistence.entity.CompetitorStatsEntity;
import com.buzzanalysis.infrastructure.persistence.mapper.CompetitorStatsMapper;
import com.buzzanalysis.infrastructure.persistence.repository.CompetitorStatsJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** {@link CompetitorStatsRepository} のJPA実装（Repositoryパターン）。既存レコードがあればIDを引き継いでupsertする。 */
@Repository
public class CompetitorStatsRepositoryImpl implements CompetitorStatsRepository {

    private final CompetitorStatsJpaRepository jpaRepository;
    private final CompetitorStatsMapper mapper;

    public CompetitorStatsRepositoryImpl(CompetitorStatsJpaRepository jpaRepository, CompetitorStatsMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public CompetitorStats save(CompetitorStats stats) {
        CompetitorStatsEntity entity = mapper.toEntity(stats);
        Optional<CompetitorStatsEntity> existing = jpaRepository.findBySocialAccountId(stats.getSocialAccountId());
        if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
            entity = mapper.toEntity(new CompetitorStats(existing.get().getId(), stats.getSocialAccountId(),
                    stats.getAverageLikeCount(), stats.getAverageCommentCount(), stats.getPostingFrequencyPerWeek(),
                    stats.getPostingTimeDistribution(), stats.getAverageVideoDurationSeconds(),
                    stats.getAverageCaptionLength(), stats.getTopPerformingPostIds(), stats.getAverageViewCount(),
                    stats.getPostFormatDistribution(), stats.getGenreDistribution(), stats.getCalculatedAt()));
        }
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<CompetitorStats> findBySocialAccountId(UUID socialAccountId) {
        return jpaRepository.findBySocialAccountId(socialAccountId).map(mapper::toDomain);
    }
}
