package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.report.Report;
import com.buzzanalysis.domain.report.ReportRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.ReportMapper;
import com.buzzanalysis.infrastructure.persistence.repository.ReportJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link ReportRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class ReportRepositoryImpl implements ReportRepository {

    private final ReportJpaRepository jpaRepository;
    private final ReportMapper mapper;

    public ReportRepositoryImpl(ReportJpaRepository jpaRepository, ReportMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Report save(Report report) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(report)));
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Report> findByPostId(UUID postId) {
        return jpaRepository.findByPostId(postId).stream().map(mapper::toDomain).toList();
    }
}
