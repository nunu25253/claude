package com.buzzanalysis.application.proposal.dto;

import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.proposal.ContentProposal;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@link ContentProposal}（ドメイン）のapplication層向けDTO。 */
public record ContentProposalDto(
        UUID id,
        UUID generationId,
        int sequenceNumber,
        String title,
        String hookPattern,
        String structureSummary,
        String callToAction,
        String targetAudience,
        String genre,
        ContentFormat recommendedFormat,
        String reasoning,
        OffsetDateTime createdAt
) {
    public static ContentProposalDto from(ContentProposal p) {
        return new ContentProposalDto(
                p.getId(), p.getGenerationId(), p.getSequenceNumber(), p.getTitle(), p.getHookPattern(),
                p.getStructureSummary(), p.getCallToAction(), p.getTargetAudience(), p.getGenre(),
                p.getRecommendedFormat(), p.getReasoning(), p.getCreatedAt()
        );
    }
}
