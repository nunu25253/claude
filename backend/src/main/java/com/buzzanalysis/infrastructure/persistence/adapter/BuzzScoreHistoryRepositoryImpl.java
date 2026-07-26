package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.score.BuzzScoreHistoryEntry;
import com.buzzanalysis.domain.score.BuzzScoreHistoryRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.BuzzScoreHistoryMapper;
import com.buzzanalysis.infrastructure.persistence.repository.BuzzScoreHistoryJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** {@link BuzzScoreHistoryRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class BuzzScoreHistoryRepositoryImpl implements BuzzScoreHistoryRepository {

    private final BuzzScoreHistoryJpaRepository jpaRepository;
    private final BuzzScoreHistoryMapper mapper;

    public BuzzScoreHistoryRepositoryImpl(BuzzScoreHistoryJpaRepository jpaRepository, BuzzScoreHistoryMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public BuzzScoreHistoryEntry save(BuzzScoreHistoryEntry entry) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(entry)));
    }

    @Override
    public List<BuzzScoreHistoryEntry> findByPostIdOrderByCalculatedAtAsc(UUID postId) {
        return jpaRepository.findByPostIdOrderByCalculatedAtAsc(postId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<UUID> findPostIdsWithAtLeastTwoEntries(int limit) {
        return jpaRepository.findPostIdsWithAtLeastTwoEntries(PageRequest.of(0, limit));
    }
}
