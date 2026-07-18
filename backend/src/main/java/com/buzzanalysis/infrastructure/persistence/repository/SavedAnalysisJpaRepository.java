package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.SavedAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link SavedAnalysisEntity} の永続化アクセス。 */
public interface SavedAnalysisJpaRepository extends JpaRepository<SavedAnalysisEntity, UUID> {

    List<SavedAnalysisEntity> findByUserId(UUID userId);
}
