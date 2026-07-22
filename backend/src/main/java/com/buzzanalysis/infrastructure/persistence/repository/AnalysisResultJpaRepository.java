package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.AnalysisResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link AnalysisResultEntity} の永続化アクセス。 */
public interface AnalysisResultJpaRepository extends JpaRepository<AnalysisResultEntity, UUID> {

    Optional<AnalysisResultEntity> findByPostId(UUID postId);

    List<AnalysisResultEntity> findByPostIdIn(List<UUID> postIds);
}
