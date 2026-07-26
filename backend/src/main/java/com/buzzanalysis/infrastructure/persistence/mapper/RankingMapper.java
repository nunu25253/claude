package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.ranking.Ranking;
import com.buzzanalysis.domain.ranking.RankingType;
import com.buzzanalysis.infrastructure.persistence.entity.RankingEntity;
import org.springframework.stereotype.Component;

/** {@link Ranking}（ドメイン）と {@link RankingEntity}（JPA）の相互変換を行う。 */
@Component
public class RankingMapper {

    private final PlatformMapper platformMapper;

    public RankingMapper(PlatformMapper platformMapper) {
        this.platformMapper = platformMapper;
    }

    public RankingEntity toEntity(Ranking ranking) {
        return new RankingEntity(
                ranking.getId(), RankingEntity.RankingTypeEnum.valueOf(ranking.getType().name()), ranking.getGenre(),
                platformMapper.toEntity(ranking.getPlatform()), ranking.getPostId(), ranking.getRankPosition(),
                ranking.getScore(), ranking.getPeriodStart(), ranking.getPeriodEnd(), ranking.getCreatedAt()
        );
    }

    public Ranking toDomain(RankingEntity entity) {
        return new Ranking(
                entity.getId(), RankingType.valueOf(entity.getType().name()), entity.getGenre(),
                platformMapper.toDomain(entity.getPlatform()), entity.getPostId(), entity.getRankPosition(),
                entity.getScore(), entity.getPeriodStart(), entity.getPeriodEnd(), entity.getCreatedAt()
        );
    }
}
