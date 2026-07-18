package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.competitor.CompetitorStats;
import com.buzzanalysis.infrastructure.persistence.entity.CompetitorStatsEntity;
import org.springframework.stereotype.Component;

/** {@link CompetitorStats}（ドメイン）と {@link CompetitorStatsEntity}（JPA）の相互変換を行う。 */
@Component
public class CompetitorStatsMapper {

    public CompetitorStatsEntity toEntity(CompetitorStats stats) {
        return new CompetitorStatsEntity(
                stats.getId(), stats.getSocialAccountId(), stats.getAverageLikeCount(), stats.getAverageCommentCount(),
                stats.getPostingFrequencyPerWeek(), stats.getPostingTimeDistribution(),
                stats.getAverageVideoDurationSeconds(), stats.getAverageCaptionLength(),
                stats.getTopPerformingPostIds(), stats.getCalculatedAt()
        );
    }

    public CompetitorStats toDomain(CompetitorStatsEntity entity) {
        return new CompetitorStats(
                entity.getId(), entity.getSocialAccountId(), entity.getAverageLikeCount(), entity.getAverageCommentCount(),
                entity.getPostingFrequencyPerWeek(), entity.getPostingTimeDistribution(),
                entity.getAverageVideoDurationSeconds(), entity.getAverageCaptionLength(),
                entity.getTopPerformingPostIds(), entity.getCalculatedAt()
        );
    }
}
