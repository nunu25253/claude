package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.score.BuzzScoreHistoryEntry;
import com.buzzanalysis.infrastructure.persistence.entity.BuzzScoreHistoryEntity;
import org.springframework.stereotype.Component;

/** {@link BuzzScoreHistoryEntry}（ドメイン）と {@link BuzzScoreHistoryEntity}（JPA）の相互変換を行う。 */
@Component
public class BuzzScoreHistoryMapper {

    public BuzzScoreHistoryEntity toEntity(BuzzScoreHistoryEntry entry) {
        return new BuzzScoreHistoryEntity(entry.getId(), entry.getPostId(), entry.getTotalScore(),
                entry.getBreakdown(), entry.getCalculatedAt());
    }

    public BuzzScoreHistoryEntry toDomain(BuzzScoreHistoryEntity entity) {
        return new BuzzScoreHistoryEntry(entity.getId(), entity.getPostId(), entity.getTotalScore(),
                entity.getBreakdown(), entity.getCalculatedAt());
    }
}
