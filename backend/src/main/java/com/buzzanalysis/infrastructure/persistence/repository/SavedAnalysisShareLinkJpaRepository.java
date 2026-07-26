package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.SavedAnalysisShareLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link SavedAnalysisShareLinkEntity} の永続化アクセス。 */
public interface SavedAnalysisShareLinkJpaRepository extends JpaRepository<SavedAnalysisShareLinkEntity, UUID> {

    List<SavedAnalysisShareLinkEntity> findBySavedAnalysisIdAndRevokedAtIsNull(UUID savedAnalysisId);
}
