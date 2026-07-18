package com.buzzanalysis.domain.rag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link RagDocument} の永続化を抽象化するリポジトリ（Repositoryパターン、Phase16）。 */
public interface RagDocumentRepository {

    RagDocument save(RagDocument document);

    Optional<RagDocument> findById(UUID id);

    /** ベクトルに近い順に上位{@code limit}件を返す（Phase4の{@code EmbeddingRepository.findNearest}と同パターン）。 */
    List<RagSimilarityMatch> findNearest(float[] queryVector, int limit);
}
