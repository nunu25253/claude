package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.savedanalysis.SavedAnalysisShareLink;
import com.buzzanalysis.infrastructure.persistence.entity.SavedAnalysisShareLinkEntity;
import org.springframework.stereotype.Component;

/** {@link SavedAnalysisShareLink}(ドメイン)と {@link SavedAnalysisShareLinkEntity}(JPA)の相互変換を行う。 */
@Component
public class SavedAnalysisShareLinkMapper {

    public SavedAnalysisShareLinkEntity toEntity(SavedAnalysisShareLink domain) {
        return new SavedAnalysisShareLinkEntity(domain.getId(), domain.getSavedAnalysisId(),
                domain.getCreatedByUserId(), domain.getCreatedAt(), domain.getRevokedAt());
    }

    public SavedAnalysisShareLink toDomain(SavedAnalysisShareLinkEntity entity) {
        return new SavedAnalysisShareLink(entity.getId(), entity.getSavedAnalysisId(), entity.getCreatedByUserId(),
                entity.getCreatedAt(), entity.getRevokedAt());
    }
}
