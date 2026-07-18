package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.infrastructure.persistence.entity.BuzzScoreEntity;
import org.springframework.stereotype.Component;

/** {@link BuzzScore}（ドメイン）と {@link BuzzScoreEntity}（JPA）の相互変換を行う。 */
@Component
public class BuzzScoreMapper {

    public BuzzScoreEntity toEntity(BuzzScore score) {
        return new BuzzScoreEntity(score.getId(), score.getPostId(), score.getTotalScore(), score.getBreakdown(), score.getCalculatedAt());
    }

    public BuzzScore toDomain(BuzzScoreEntity entity) {
        return new BuzzScore(entity.getId(), entity.getPostId(), entity.getTotalScore(), entity.getBreakdown(), entity.getCalculatedAt());
    }
}
