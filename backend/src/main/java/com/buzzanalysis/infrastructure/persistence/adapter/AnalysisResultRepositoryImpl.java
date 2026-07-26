package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.AnalysisResultMapper;
import com.buzzanalysis.infrastructure.persistence.repository.AnalysisResultJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link AnalysisResultRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class AnalysisResultRepositoryImpl implements AnalysisResultRepository {

    private final AnalysisResultJpaRepository jpaRepository;
    private final AnalysisResultMapper mapper;

    public AnalysisResultRepositoryImpl(AnalysisResultJpaRepository jpaRepository, AnalysisResultMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public AnalysisResult save(AnalysisResult result) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(result)));
    }

    @Override
    public Optional<AnalysisResult> findByPostId(UUID postId) {
        return jpaRepository.findByPostId(postId).map(mapper::toDomain);
    }

    @Override
    public List<AnalysisResult> findByPostIdIn(List<UUID> postIds) {
        return jpaRepository.findByPostIdIn(postIds).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<AnalysisResult> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
