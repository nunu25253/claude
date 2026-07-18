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
     * {@code EmbeddingJpaRepository.findNearest}と同パターン）。
     */
    @Query(value = """
            SELECT d.id AS documentId, (1 - (d.vector <=> CAST(:queryVector AS vector))) AS similarity
            FROM rag_documents d
            ORDER BY d.vector <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<RagDocumentSimilarityRow> findNearest(@Param("queryVector") String queryVector, @Param("limit") int limit);
}
