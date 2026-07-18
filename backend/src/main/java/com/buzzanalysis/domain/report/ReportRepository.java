package com.buzzanalysis.domain.report;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Report集約のリポジトリインターフェース。 */
public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(UUID id);

    List<Report> findByPostId(UUID postId);
}
