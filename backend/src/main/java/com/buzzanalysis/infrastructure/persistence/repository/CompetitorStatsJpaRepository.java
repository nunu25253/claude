package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.CompetitorStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link CompetitorStatsEntity} の永続化アクセス。 */
public interface CompetitorStatsJpaRepository extends JpaRepository<CompetitorStatsEntity, UUID> {

    Optional<CompetitorStatsEntity> findBySocialAccountId(UUID socialAccountId);
}
