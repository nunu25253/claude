package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.embedding.Embedding;
import com.buzzanalysis.domain.embedding.EmbeddingRepository;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import com.buzzanalysis.domain.embedding.SimilarityMatch;
import com.buzzanalysis.infrastructure.persistence.entity.EmbeddingEntity;
import com.buzzanalysis.infrastructure.persistence.mapper.EmbeddingMapper;
import com.buzzanalysis.infrastructure.persistence.repository.EmbeddingJpaRepository;
import com.buzzanalysis.infrastructure.persistence.type.VectorType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * {@link EmbeddingRepository} のJPA実装（Repositoryパターン）。{@code post_id + target} の一意制約を
 * 尊重し、既存レコードがあれば同じ行を更新する（新しいUUIDでの重複INSERTを避けるupsertセマンティクス）。
 */
@Repository
public class EmbeddingRepositoryImpl implements EmbeddingRepository {

    private final EmbeddingJpaRepository jpaRepository;
    private final EmbeddingMapper mapper;

    public EmbeddingRepositoryImpl(EmbeddingJpaRepository jpaRepository, EmbeddingMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Embedding save(Embedding embedding) {
        EmbeddingEntity.EmbeddingTargetEnum targetEnum =
                EmbeddingEntity.EmbeddingTargetEnum.valueOf(embedding.getTarget().name());
        Optional<EmbeddingEntity> existing = jpaRepository.findByPostIdAndTarget(embedding.getPostId(), targetEnum);

        EmbeddingEntity toSave;
        if (existing.isPresent()) {
            EmbeddingEntity entity = existing.get();
            entity.update(embedding.getVector(), embedding.getSourceText(), embedding.getModel(),
                    embedding.getDimensions(), embedding.getGeneratedAt());
            toSave = entity;
        } else {
            toSave = mapper.toEntity(embedding);
        }
        return mapper.toDomain(jpaRepository.save(toSave));
    }

    @Override
    public Optional<Embedding> findByPostIdAndTarget(UUID postId, EmbeddingTarget target) {
        return jpaRepository.findByPostIdAndTarget(postId, EmbeddingEntity.EmbeddingTargetEnum.valueOf(target.name()))
                .map(mapper::toDomain);
    }

    @Override
    public List<Embedding> findAllByPostId(UUID postId) {
        return jpaRepository.findAllByPostId(postId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<SimilarityMatch> findNearest(EmbeddingTarget target, float[] queryVector, int limit) {
        String queryVectorLiteral = VectorType.toVectorLiteral(queryVector);
        return jpaRepository.findNearest(queryVectorLiteral, target.name(), limit).stream()
                .map(row -> new SimilarityMatch(row.getPostId(), row.getSimilarity()))
                .toList();
    }
}
