package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.BuzzScoreHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link BuzzScoreHistoryEntity} の永続化アクセス。 */
public interface BuzzScoreHistoryJpaRepository extends JpaRepository<BuzzScoreHistoryEntity, UUID> {

    List<BuzzScoreHistoryEntity> findByPostIdOrderByCalculatedAtAsc(UUID postId);
}
