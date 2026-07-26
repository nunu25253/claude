package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.ContentEvaluationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link ContentEvaluationEntity} の永続化アクセス。 */
public interface ContentEvaluationJpaRepository extends JpaRepository<ContentEvaluationEntity, UUID> {

    List<ContentEvaluationEntity> findByProposalId(UUID proposalId);
}
