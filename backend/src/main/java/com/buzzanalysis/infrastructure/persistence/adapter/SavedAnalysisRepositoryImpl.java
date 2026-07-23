package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.SavedAnalysisMapper;
import com.buzzanalysis.infrastructure.persistence.repository.SavedAnalysisJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link SavedAnalysisRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class SavedAnalysisRepositoryImpl implements SavedAnalysisRepository {

    private final SavedAnalysisJpaRepository jpaRepository;
    private final SavedAnalysisMapper mapper;

    public SavedAnalysisRepositoryImpl(SavedAnalysisJpaRepository jpaRepository, SavedAnalysisMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SavedAnalysis save(SavedAnalysis savedAnalysis) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(savedAnalysis)));
    }

    @Override
    public List<SavedAnalysis> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<SavedAnalysis> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<SavedAnalysis> findPendingAlerts() {
        return jpaRepository.findByAlertThresholdIsNotNullAndAlertTriggeredAtIsNull().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
