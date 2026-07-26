package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.embedding.Embedding;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import com.buzzanalysis.infrastructure.persistence.entity.EmbeddingEntity;
import org.springframework.stereotype.Component;

/** {@link Embedding}（ドメイン）と {@link EmbeddingEntity}（JPA）の相互変換を行う。 */
@Component
public class EmbeddingMapper {

    public EmbeddingEntity toEntity(Embedding embedding) {
        return new EmbeddingEntity(
                embedding.getId(), embedding.getPostId(),
                EmbeddingEntity.EmbeddingTargetEnum.valueOf(embedding.getTarget().name()),
                embedding.getVector(), embedding.getSourceText(), embedding.getModel(),
                embedding.getDimensions(), embedding.getGeneratedAt()
        );
    }

    public Embedding toDomain(EmbeddingEntity entity) {
        return new Embedding(
                entity.getId(), entity.getPostId(), EmbeddingTarget.valueOf(entity.getTarget().name()),
                entity.getVector(), entity.getSourceText(), entity.getModel(), entity.getDimensions(),
                entity.getGeneratedAt()
        );
    }
}
