package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.rag.RagDocument;
import com.buzzanalysis.domain.rag.RagDocumentRepository;
import com.buzzanalysis.domain.rag.RagSimilarityMatch;
import com.buzzanalysis.infrastructure.persistence.mapper.RagDocumentMapper;
import com.buzzanalysis.infrastructure.persistence.repository.RagDocumentJpaRepository;
import com.buzzanalysis.infrastructure.persistence.type.VectorType;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link RagDocumentRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class RagDocumentRepositoryImpl implements RagDocumentRepository {

    private final RagDocumentJpaRepository jpaRepository;
    private final RagDocumentMapper mapper;

    public RagDocumentRepositoryImpl(RagDocumentJpaRepository jpaRepository, RagDocumentMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public RagDocument save(RagDocument document) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(document)));
    }

    @Override
    public Optional<RagDocument> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<RagDocument> findByIdIn(List<UUID> ids) {
        return jpaRepository.findAllById(ids).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<RagSimilarityMatch> findNearest(float[] queryVector, UUID userId, int limit) {
        String queryVectorLiteral = VectorType.toVectorLiteral(queryVector);
        return jpaRepository.findNearest(queryVectorLiteral, userId, limit).stream()
                .map(row -> new RagSimilarityMatch(row.getDocumentId(), row.getSimilarity()))
                .toList();
    }
}
