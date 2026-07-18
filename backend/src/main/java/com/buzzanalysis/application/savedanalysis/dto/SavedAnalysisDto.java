package com.buzzanalysis.application.savedanalysis.dto;

import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;

import java.time.OffsetDateTime;
import java.util.UUID;

/** SavedAnalysis集約のapplication層向けDTO。 */
public record SavedAnalysisDto(UUID id, UUID userId, UUID postId, String note, OffsetDateTime createdAt) {

    public static SavedAnalysisDto from(SavedAnalysis entity) {
        return new SavedAnalysisDto(entity.getId(), entity.getUserId(), entity.getPostId(), entity.getNote(), entity.getCreatedAt());
    }
}
