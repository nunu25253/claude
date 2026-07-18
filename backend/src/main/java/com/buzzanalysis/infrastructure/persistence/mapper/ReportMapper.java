package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.report.Report;
import com.buzzanalysis.domain.report.ReportFormat;
import com.buzzanalysis.infrastructure.persistence.entity.ReportEntity;
import org.springframework.stereotype.Component;

/** {@link Report}（ドメイン）と {@link ReportEntity}（JPA）の相互変換を行う。 */
@Component
public class ReportMapper {

    public ReportEntity toEntity(Report report) {
        return new ReportEntity(
                report.getId(), report.getPostId(), ReportEntity.ReportFormatEnum.valueOf(report.getFormat().name()),
                report.getTitle(), report.getStorageKey(), report.getContentSizeBytes(), report.getGeneratedAt()
        );
    }

    public Report toDomain(ReportEntity entity) {
        return Report.builder()
                .id(entity.getId())
                .postId(entity.getPostId())
                .format(ReportFormat.valueOf(entity.getFormat().name()))
                .title(entity.getTitle())
                .storageKey(entity.getStorageKey())
                .contentSizeBytes(entity.getContentSizeBytes())
                .generatedAt(entity.getGeneratedAt())
                .build();
    }
}
