package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.proposal.ContentProposal;
import com.buzzanalysis.domain.proposal.ContentProposalRepository;
import com.buzzanalysis.infrastructure.persistence.entity.ContentProposalEntity;
import com.buzzanalysis.infrastructure.persistence.mapper.ContentProposalMapper;
import com.buzzanalysis.infrastructure.persistence.repository.ContentProposalJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** {@link ContentProposalRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class ContentProposalRepositoryImpl implements ContentProposalRepository {

    private final ContentProposalJpaRepository jpaRepository;
    private final ContentProposalMapper mapper;

    public ContentProposalRepositoryImpl(ContentProposalJpaRepository jpaRepository, ContentProposalMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<ContentProposal> saveAll(List<ContentProposal> proposals) {
        List<ContentProposalEntity> entities = proposals.stream().map(mapper::toEntity).toList();
        return jpaRepository.saveAll(entities).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<ContentProposal> findByGenerationId(UUID generationId) {
        return jpaRepository.findByGenerationIdOrderBySequenceNumberAsc(generationId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
