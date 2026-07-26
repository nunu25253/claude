package com.buzzanalysis.application.rag.dto;

import com.buzzanalysis.domain.rag.RagDocument;
import com.buzzanalysis.domain.rag.RagSourceType;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@link RagDocument}（ドメイン）のapplication層向けDTO。ベクトル本体は含めない。 */
public record RagDocumentDto(UUID id, RagSourceType sourceType, UUID sourceId, String contentText,
                              OffsetDateTime createdAt) {
    public static RagDocumentDto from(RagDocument d) {
        return new RagDocumentDto(d.getId(), d.getSourceType(), d.getSourceId(), d.getContentText(),
                d.getCreatedAt());
    }
}
