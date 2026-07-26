package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.trend.TrendReport;
import com.buzzanalysis.domain.trend.TrendReportRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.TrendReportMapper;
import com.buzzanalysis.infrastructure.persistence.repository.TrendReportJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

/** {@link TrendReportRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class TrendReportRepositoryImpl implements TrendReportRepository {

    private final TrendReportJpaRepository jpaRepository;
    private final TrendReportMapper mapper;

    public TrendReportRepositoryImpl(TrendReportJpaRepository jpaRepository, TrendReportMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public TrendReport save(TrendReport report) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(report)));
    }

    @Override
    public List<TrendReport> findLatest(Platform platform, int limit) {
        return jpaRepository.findLatestByPlatform(platform, PageRequest.of(0, limit)).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
