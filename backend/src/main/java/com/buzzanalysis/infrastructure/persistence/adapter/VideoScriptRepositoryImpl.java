package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.script.VideoScript;
import com.buzzanalysis.domain.script.VideoScriptRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.VideoScriptMapper;
import com.buzzanalysis.infrastructure.persistence.repository.VideoScriptJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link VideoScriptRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class VideoScriptRepositoryImpl implements VideoScriptRepository {

    private final VideoScriptJpaRepository jpaRepository;
    private final VideoScriptMapper mapper;

    public VideoScriptRepositoryImpl(VideoScriptJpaRepository jpaRepository, VideoScriptMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public VideoScript save(VideoScript script) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(script)));
    }

    @Override
    public List<VideoScript> findByProposalId(UUID proposalId) {
        return jpaRepository.findByProposalId(proposalId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<VideoScript> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
