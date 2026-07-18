package com.buzzanalysis.application.report.dto;

import com.buzzanalysis.domain.report.Report;
import com.buzzanalysis.domain.report.ReportFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

/** レポート履歴一覧（{@code GET /reports}）の1件分。フロントエンドの{@code ReportHistoryItem}型に対応する。 */
public record ReportHistoryItemDto(
        UUID reportId,
        UUID postId,
        String postCaption,
        ReportFormat format,
        OffsetDateTime createdAt,
        String downloadUrl
) {
    public static ReportHistoryItemDto from(Report report, String postCaption, String downloadUrl) {
        return new ReportHistoryItemDto(report.getId(), report.getPostId(), postCaption, report.getFormat(),
                report.getGeneratedAt(), downloadUrl);
    }
}
