package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.PlatformEnum;
import com.buzzanalysis.infrastructure.persistence.entity.RankingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Spring Data JPAによる {@link RankingEntity} の永続化アクセス。 */
public interface RankingJpaRepository extends JpaRepository<RankingEntity, java.util.UUID> {

    // genre/platform 未指定時は「絞り込みなし(全ジャンル/全プラットフォームを1つに束ねた集計行 = 該当カラムがnull)」
    // のみを返す。仮に「未指定なら値を問わず何でも一致」としてしまうと、
    // 全体版の行とジャンル別/プラットフォーム別の行が同じ結果セットに混在し、同一投稿が重複して返る。
    @Query("""
            select r from RankingEntity r
            where r.type = :type
              and ((:genre is null and r.genre is null) or r.genre = :genre)
              and ((:platform is null and r.platform is null) or r.platform = :platform)
            order by r.rankPosition asc
            """)
    List<RankingEntity> findByFilters(@Param("type") RankingEntity.RankingTypeEnum type,
                                       @Param("genre") String genre,
                                       @Param("platform") PlatformEnum platform,
                                       org.springframework.data.domain.Pageable pageable);

    void deleteByType(RankingEntity.RankingTypeEnum type);
}
