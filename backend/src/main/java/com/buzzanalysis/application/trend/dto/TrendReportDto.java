package com.buzzanalysis.application.trend.dto;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.trend.TrendReport;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@link TrendReport}（ドメイン）のapplication層向けDTO。 */
public record TrendReportDto(
        UUID id,
        Platform platform,
        int recentWindowDays,
        int baselineWindowDays,
        List<TrendItemDto> items,
        String aiSummary,
        OffsetDateTime createdAt
) {
    public static TrendReportDto from(TrendReport r) {
        return new TrendReportDto(
                r.getId(), r.getPlatform(), r.getRecentWindowDays(), r.getBaselineWindowDays(),
                r.getItems().stream().map(TrendItemDto::from).toList(), r.getAiSummary(), r.getCreatedAt()
        );
    }
}
