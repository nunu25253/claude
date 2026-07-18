package com.buzzanalysis.application.evaluation.dto;

import com.buzzanalysis.domain.evaluation.ContentEvaluation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@link ContentEvaluation}（ドメイン）のapplication層向けDTO。 */
public record ContentEvaluationDto(
        UUID id,
        UUID proposalId,
        String title,
        Double matchRatePercent,
        String targetAudienceEstimate,
        List<String> improvementSuggestions,
        String hookImprovement,
        String ctaImprovement,
        int predictedScore,
        OffsetDateTime createdAt
) {
    public static ContentEvaluationDto from(ContentEvaluation e) {
        return new ContentEvaluationDto(
                e.getId(), e.getProposalId(), e.getTitle(), e.getMatchRatePercent(), e.getTargetAudienceEstimate(),
                e.getImprovementSuggestions(), e.getHookImprovement(), e.getCtaImprovement(), e.getPredictedScore(),
                e.getCreatedAt()
        );
    }
}
