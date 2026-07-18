package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.infrastructure.persistence.entity.TrendReportEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPAによる {@link TrendReportEntity} の永続化アクセス。
 * {@code platform}がnullの場合はplatform列がnullのレポート（全プラットフォーム対象）のみを検索する。
 * JPQLの{@code =}はnullとの比較でUNKNOWNになるため、明示的に{@code IS NULL}分岐を行う。
 */
public interface TrendReportJpaRepository extends JpaRepository<TrendReportEntity, UUID> {

    @Query("SELECT e FROM TrendReportEntity e WHERE (:platform IS NULL AND e.platform IS NULL) "
            + "OR e.platform = :platform ORDER BY e.createdAt DESC")
    List<TrendReportEntity> findLatestByPlatform(@Param("platform") Platform platform, Pageable pageable);
}
