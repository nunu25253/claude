package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.ranking.Ranking;
import com.buzzanalysis.domain.ranking.RankingRepository;
import com.buzzanalysis.domain.ranking.RankingType;
import com.buzzanalysis.infrastructure.persistence.entity.RankingEntity;
import com.buzzanalysis.infrastructure.persistence.mapper.PlatformMapper;
import com.buzzanalysis.infrastructure.persistence.mapper.RankingMapper;
import com.buzzanalysis.infrastructure.persistence.repository.RankingJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

/** {@link RankingRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class RankingRepositoryImpl implements RankingRepository {

    private final RankingJpaRepository jpaRepository;
    private final RankingMapper mapper;
    private final PlatformMapper platformMapper;

    public RankingRepositoryImpl(RankingJpaRepository jpaRepository, RankingMapper mapper, PlatformMapper platformMapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.platformMapper = platformMapper;
    }

    @Override
    public List<Ranking> save(List<Ranking> rankings) {
        List<RankingEntity> entities = rankings.stream().map(mapper::toEntity).toList();
        return jpaRepository.saveAll(entities).stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteByType(RankingType type) {
        jpaRepository.deleteByType(RankingEntity.RankingTypeEnum.valueOf(type.name()));
    }

    @Override
    public List<Ranking> findByFilters(RankingType type, String genre, Platform platform, int limit) {
        RankingEntity.RankingTypeEnum typeEnum = RankingEntity.RankingTypeEnum.valueOf(type.name());
        var entities = jpaRepository.findByFilters(typeEnum, genre, platformMapper.toEntity(platform),
                PageRequest.of(0, Math.max(1, limit)));
        return entities.stream().map(mapper::toDomain).toList();
    }
}
