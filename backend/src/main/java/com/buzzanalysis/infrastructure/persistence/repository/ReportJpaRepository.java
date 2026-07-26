package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link ReportEntity} の永続化アクセス。 */
public interface ReportJpaRepository extends JpaRepository<ReportEntity, UUID> {

    List<ReportEntity> findByPostId(UUID postId);

    List<ReportEntity> findByUserIdOrderByGeneratedAtDesc(UUID userId);
}
