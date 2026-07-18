package com.buzzanalysis.domain.embedding;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Embedding集約のリポジトリインターフェース。 */
public interface EmbeddingRepository {

    Embedding save(Embedding embedding);

    Optional<Embedding> findByPostIdAndTarget(UUID postId, EmbeddingTarget target);

    List<Embedding> findAllByPostId(UUID postId);
}
