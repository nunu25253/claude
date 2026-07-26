package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.BuzzScoreHistoryEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link BuzzScoreHistoryEntity} の永続化アクセス。 */
public interface BuzzScoreHistoryJpaRepository extends JpaRepository<BuzzScoreHistoryEntity, UUID> {

    List<BuzzScoreHistoryEntity> findByPostIdOrderByCalculatedAtAsc(UUID postId);

    @Query("SELECT h.postId FROM BuzzScoreHistoryEntity h GROUP BY h.postId HAVING COUNT(h) >= 2 "
            + "ORDER BY MAX(h.calculatedAt) DESC")
    List<UUID> findPostIdsWithAtLeastTwoEntries(Pageable pageable);
}
