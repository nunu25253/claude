package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.infrastructure.persistence.entity.SavedAnalysisEntity;
import org.springframework.stereotype.Component;

/** {@link SavedAnalysis}（ドメイン）と {@link SavedAnalysisEntity}（JPA）の相互変換を行う。 */
@Component
public class SavedAnalysisMapper {

    public SavedAnalysisEntity toEntity(SavedAnalysis entity) {
        return new SavedAnalysisEntity(entity.getId(), entity.getUserId(), entity.getPostId(), entity.getNote(),
                entity.getCreatedAt(), entity.getAlertThreshold(), entity.getAlertTriggeredAt());
    }

    public SavedAnalysis toDomain(SavedAnalysisEntity entity) {
        return new SavedAnalysis(entity.getId(), entity.getUserId(), entity.getPostId(), entity.getNote(),
                entity.getCreatedAt(), entity.getAlertThreshold(), entity.getAlertTriggeredAt());
    }
}
