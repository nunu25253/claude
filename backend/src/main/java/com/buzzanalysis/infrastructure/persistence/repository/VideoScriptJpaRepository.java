package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.VideoScriptEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link VideoScriptEntity} の永続化アクセス。 */
public interface VideoScriptJpaRepository extends JpaRepository<VideoScriptEntity, UUID> {

    List<VideoScriptEntity> findByProposalId(UUID proposalId);
}
