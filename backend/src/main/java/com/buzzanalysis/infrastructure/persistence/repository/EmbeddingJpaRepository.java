package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.EmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link EmbeddingEntity} の永続化アクセス。 */
public interface EmbeddingJpaRepository extends JpaRepository<EmbeddingEntity, UUID> {

    Optional<EmbeddingEntity> findByPostIdAndTarget(UUID postId, EmbeddingEntity.EmbeddingTargetEnum target);

    List<EmbeddingEntity> findAllByPostId(UUID postId);

    /**
     * pgvectorのコサイン距離演算子（{@code <=>}）を用いた類似検索（Phase4）。JPQLでは表現できないため
     * ネイティブクエリを使用する。{@code queryVector} はpgvectorのテキスト表現
     * （例: "[0.1,0.2,0.3]"、{@code VectorType.toVectorLiteral} で生成）を渡す。
     */
    @Query(value = """
            SELECT e.post_id AS postId, (1 - (e.vector <=> CAST(:queryVector AS vector))) AS similarity
            FROM embeddings e
            WHERE e.target = :target
            ORDER BY e.vector <=> CAST(:queryVector AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<EmbeddingSimilarityRow> findNearest(@Param("queryVector") String queryVector,
                                              @Param("target") String target,
                                              @Param("limit") int limit);
}
