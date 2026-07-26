package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.infrastructure.persistence.entity.ContentProposalEntity;
import org.springframework.stereotype.Component;

/** {@link ContentProposal}（ドメイン）と {@link ContentProposalEntity}（JPA）の相互変換を行う。 */
@Component
public class ContentProposalMapper {

    public ContentProposalEntity toEntity(ContentProposal p) {
        return new ContentProposalEntity(
                p.getId(), p.getGenerationId(), p.getSequenceNumber(), p.getTitle(), p.getHookPattern(),
                p.getStructureSummary(), p.getCallToAction(), p.getTargetAudience(), p.getGenre(),
                p.getRecommendedFormat(), p.getReasoning(), p.getCreatedAt()
        );
    }

    public ContentProposal toDomain(ContentProposalEntity entity) {
        return ContentProposal.builder()
                .id(entity.getId())
                .generationId(entity.getGenerationId())
                .sequenceNumber(entity.getSequenceNumber())
                .title(entity.getTitle())
                .hookPattern(entity.getHookPattern())
                .structureSummary(entity.getStructureSummary())
                .callToAction(entity.getCallToAction())
                .targetAudience(entity.getTargetAudience())
                .genre(entity.getGenre())
                .recommendedFormat(entity.getRecommendedFormat())
                .reasoning(entity.getReasoning())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
