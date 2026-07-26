package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.trend.TrendReport;
import com.buzzanalysis.infrastructure.persistence.entity.TrendReportEntity;
import org.springframework.stereotype.Component;

/** {@link TrendReport}（ドメイン）と {@link TrendReportEntity}（JPA）の相互変換を行う。 */
@Component
public class TrendReportMapper {

    public TrendReportEntity toEntity(TrendReport r) {
        return new TrendReportEntity(r.getId(), r.getPlatform(), r.getRecentWindowDays(), r.getBaselineWindowDays(),
                r.getItems(), r.getAiSummary(), r.getCreatedAt());
    }

    public TrendReport toDomain(TrendReportEntity entity) {
        return TrendReport.builder()
                .id(entity.getId())
                .platform(entity.getPlatform())
                .recentWindowDays(entity.getRecentWindowDays())
                .baselineWindowDays(entity.getBaselineWindowDays())
                .items(entity.getItems())
                .aiSummary(entity.getAiSummary())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
