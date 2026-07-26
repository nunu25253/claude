package com.buzzanalysis.domain.rag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link RagDocument} の永続化を抽象化するリポジトリ（Repositoryパターン、Phase16）。 */
public interface RagDocumentRepository {

    RagDocument save(RagDocument document);

    Optional<RagDocument> findById(UUID id);

    List<RagDocument> findByIdIn(List<UUID> ids);

    /**
     * ベクトルに近い順に上位{@code limit}件を返す(Phase4の{@code EmbeddingRepository.findNearest}と同パターン)。
     * {@code userId}が索引登録したドキュメントのみを対象とする(他ユーザーの分析結果が根拠として
     * 漏洩しないようにするための必須の絞り込み)。
     */
    List<RagSimilarityMatch> findNearest(float[] queryVector, UUID userId, int limit);
}
