package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.EmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link EmbeddingEntity} の永続化アクセス。 */
public interface EmbeddingJpaRepository extends JpaRepository<EmbeddingEntity, UUID> {

    Optional<EmbeddingEntity> findByPostIdAndTarget(UUID postId, EmbeddingEntity.EmbeddingTargetEnum target);

    List<EmbeddingEntity> findAllByPostId(UUID postId);
}
