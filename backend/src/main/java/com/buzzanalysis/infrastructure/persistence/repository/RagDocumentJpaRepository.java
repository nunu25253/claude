package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.RagDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link RagDocumentEntity} の永続化アクセス。 */
public interface RagDocumentJpaRepository extends JpaRepository<RagDocumentEntity, UUID> {

    /**
     * pgvectorのコサイン距離演算子（{@code <=>}）を用いた類似検索（Phase16、
     * {@code EmbeddingJpaRepository.findNearest}と同パターン）。他ユーザーの分析結果が根拠として
     * 漏洩しないよう、必ずuser_idで絞り込む。
     */
    @Query(value = """
            SELECT d.id AS documentId, (1 - (d.vector <=> CAST(:queryVector AS vector))) AS similarity
            FROM rag_documents d
            WHERE d.user_id = :userId
            ORDER BY d.vector <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<RagDocumentSimilarityRow> findNearest(@Param("queryVector") String queryVector,
                                                @Param("userId") UUID userId, @Param("limit") int limit);
}
