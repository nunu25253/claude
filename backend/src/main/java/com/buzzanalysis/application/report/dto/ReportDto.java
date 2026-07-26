package com.buzzanalysis.application.report.dto;

import com.buzzanalysis.domain.report.Report;
import com.buzzanalysis.domain.report.ReportFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Report集約のapplication層向けDTO。ダウンロード用の一時URLを含む。 */
public record ReportDto(
        UUID id,
        UUID postId,
        ReportFormat format,
        String title,
        long contentSizeBytes,
        String downloadUrl,
        OffsetDateTime generatedAt
) {
    public static ReportDto from(Report report, String downloadUrl) {
        return new ReportDto(report.getId(), report.getPostId(), report.getFormat(), report.getTitle(),
                report.getContentSizeBytes(), downloadUrl, report.getGeneratedAt());
    }
}
