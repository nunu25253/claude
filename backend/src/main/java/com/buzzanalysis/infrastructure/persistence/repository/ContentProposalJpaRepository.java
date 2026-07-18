package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.ContentProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link ContentProposalEntity} の永続化アクセス。 */
public interface ContentProposalJpaRepository extends JpaRepository<ContentProposalEntity, UUID> {

    List<ContentProposalEntity> findByGenerationIdOrderBySequenceNumberAsc(UUID generationId);
}
