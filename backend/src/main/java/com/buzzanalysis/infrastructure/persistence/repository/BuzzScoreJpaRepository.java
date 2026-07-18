package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.BuzzScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link BuzzScoreEntity} の永続化アクセス。 */
public interface BuzzScoreJpaRepository extends JpaRepository<BuzzScoreEntity, UUID> {

    Optional<BuzzScoreEntity> findByPostId(UUID postId);
}
