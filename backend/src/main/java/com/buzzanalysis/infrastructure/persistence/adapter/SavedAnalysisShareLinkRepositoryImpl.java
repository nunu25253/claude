package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.savedanalysis.SavedAnalysisShareLink;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisShareLinkRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.SavedAnalysisShareLinkMapper;
import com.buzzanalysis.infrastructure.persistence.repository.SavedAnalysisShareLinkJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** {@link SavedAnalysisShareLinkRepository} のJPA実装(Repositoryパターン)。 */
@Repository
public class SavedAnalysisShareLinkRepositoryImpl implements SavedAnalysisShareLinkRepository {

    private final SavedAnalysisShareLinkJpaRepository jpaRepository;
    private final SavedAnalysisShareLinkMapper mapper;

    public SavedAnalysisShareLinkRepositoryImpl(SavedAnalysisShareLinkJpaRepository jpaRepository,
                                                 SavedAnalysisShareLinkMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public SavedAnalysisShareLink save(SavedAnalysisShareLink link) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(link)));
    }

    @Override
    public Optional<SavedAnalysisShareLink> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SavedAnalysisShareLink> findActiveBySavedAnalysisId(UUID savedAnalysisId) {
        return jpaRepository.findBySavedAnalysisIdAndRevokedAtIsNull(savedAnalysisId).stream()
                .map(mapper::toDomain)
                .findFirst();
    }
}
