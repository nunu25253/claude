package com.buzzanalysis.domain.report;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Report集約のリポジトリインターフェース。 */
public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(UUID id);

    List<Report> findByPostId(UUID postId);

    /** 指定ユーザーが生成したレポートを新しい順に返す（履歴一覧用）。 */
    List<Report> findByUserIdOrderByGeneratedAtDesc(UUID userId);
}
