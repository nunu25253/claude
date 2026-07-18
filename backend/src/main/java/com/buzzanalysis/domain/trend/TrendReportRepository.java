package com.buzzanalysis.domain.trend;

import com.buzzanalysis.domain.platform.Platform;

import java.util.List;

/** {@link TrendReport} の永続化を抽象化するリポジトリ（Repositoryパターン、Phase15）。 */
public interface TrendReportRepository {

    TrendReport save(TrendReport report);

    /** {@code platform}がnullの場合は全プラットフォーム分（platform=nullで保存されたレポート）を対象とする。 */
    List<TrendReport> findLatest(Platform platform, int limit);
}
