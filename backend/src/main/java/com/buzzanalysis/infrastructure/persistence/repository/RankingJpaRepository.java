package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.PlatformEnum;
import com.buzzanalysis.infrastructure.persistence.entity.RankingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Spring Data JPAによる {@link RankingEntity} の永続化アクセス。 */
public interface RankingJpaRepository extends JpaRepository<RankingEntity, java.util.UUID> {

    @Query("""
            select r from RankingEntity r
            where r.type = :type
              and (:genre is null or r.genre = :genre)
              and (:platform is null or r.platform = :platform)
            order by r.rankPosition asc
            """)
    List<RankingEntity> findByFilters(@Param("type") RankingEntity.RankingTypeEnum type,
                                       @Param("genre") String genre,
                                       @Param("platform") PlatformEnum platform,
                                       org.springframework.data.domain.Pageable pageable);

    void deleteByType(RankingEntity.RankingTypeEnum type);
}
