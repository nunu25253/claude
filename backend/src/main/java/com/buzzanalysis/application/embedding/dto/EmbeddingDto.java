package com.buzzanalysis.application.embedding.dto;

import com.buzzanalysis.domain.embedding.Embedding;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@link Embedding}（ドメイン）のapplication層向けDTO。 */
public record EmbeddingDto(
        UUID id,
        UUID postId,
        EmbeddingTarget target,
        float[] vector,
        String model,
        int dimensions,
        OffsetDateTime generatedAt
) {
    public static EmbeddingDto from(Embedding embedding) {
        return new EmbeddingDto(embedding.getId(), embedding.getPostId(), embedding.getTarget(),
                embedding.getVector(), embedding.getModel(), embedding.getDimensions(), embedding.getGeneratedAt());
    }
}
