package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.BuzzScoreMapper;
import com.buzzanalysis.infrastructure.persistence.repository.BuzzScoreJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** {@link BuzzScoreRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class BuzzScoreRepositoryImpl implements BuzzScoreRepository {

    private final BuzzScoreJpaRepository jpaRepository;
    private final BuzzScoreMapper mapper;

    public BuzzScoreRepositoryImpl(BuzzScoreJpaRepository jpaRepository, BuzzScoreMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public BuzzScore save(BuzzScore buzzScore) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(buzzScore)));
    }

    @Override
    public Optional<BuzzScore> findByPostId(UUID postId) {
        return jpaRepository.findByPostId(postId).map(mapper::toDomain);
    }
}
