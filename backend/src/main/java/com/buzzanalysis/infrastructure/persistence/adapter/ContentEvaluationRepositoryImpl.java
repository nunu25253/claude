package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.evaluation.ContentEvaluation;
import com.buzzanalysis.domain.evaluation.ContentEvaluationRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.ContentEvaluationMapper;
import com.buzzanalysis.infrastructure.persistence.repository.ContentEvaluationJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** {@link ContentEvaluationRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class ContentEvaluationRepositoryImpl implements ContentEvaluationRepository {

    private final ContentEvaluationJpaRepository jpaRepository;
    private final ContentEvaluationMapper mapper;

    public ContentEvaluationRepositoryImpl(ContentEvaluationJpaRepository jpaRepository,
                                            ContentEvaluationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ContentEvaluation save(ContentEvaluation evaluation) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(evaluation)));
    }

    @Override
    public List<ContentEvaluation> findByProposalId(UUID proposalId) {
        return jpaRepository.findByProposalId(proposalId).stream().map(mapper::toDomain).toList();
    }
}
