package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.evaluation.ContentEvaluation;
import com.buzzanalysis.infrastructure.persistence.entity.ContentEvaluationEntity;
import org.springframework.stereotype.Component;

/** {@link ContentEvaluation}（ドメイン）と {@link ContentEvaluationEntity}（JPA）の相互変換を行う。 */
@Component
public class ContentEvaluationMapper {

    public ContentEvaluationEntity toEntity(ContentEvaluation e) {
        return new ContentEvaluationEntity(e.getId(), e.getProposalId(), e.getTitle(), e.getMatchRatePercent(),
                e.getTargetAudienceEstimate(), e.getImprovementSuggestions(), e.getHookImprovement(),
                e.getCtaImprovement(), e.getPredictedScore(), e.getCreatedAt());
    }

    public ContentEvaluation toDomain(ContentEvaluationEntity entity) {
        return ContentEvaluation.builder()
                .id(entity.getId())
                .proposalId(entity.getProposalId())
                .title(entity.getTitle())
                .matchRatePercent(entity.getMatchRatePercent())
                .targetAudienceEstimate(entity.getTargetAudienceEstimate())
                .improvementSuggestions(entity.getImprovementSuggestions())
                .hookImprovement(entity.getHookImprovement())
                .ctaImprovement(entity.getCtaImprovement())
                .predictedScore(entity.getPredictedScore())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
