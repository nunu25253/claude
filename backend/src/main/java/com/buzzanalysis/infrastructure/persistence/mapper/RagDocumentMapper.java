package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.rag.RagDocument;
import com.buzzanalysis.infrastructure.persistence.entity.RagDocumentEntity;
import org.springframework.stereotype.Component;

/** {@link RagDocument}（ドメイン）と {@link RagDocumentEntity}（JPA）の相互変換を行う。 */
@Component
public class RagDocumentMapper {

    public RagDocumentEntity toEntity(RagDocument d) {
        return new RagDocumentEntity(d.getId(), d.getSourceType(), d.getSourceId(), d.getContentText(),
                d.getVector(), d.getModel(), d.getDimensions(), d.getCreatedAt());
    }

    public RagDocument toDomain(RagDocumentEntity entity) {
        return RagDocument.builder()
                .id(entity.getId())
                .sourceType(entity.getSourceType())
                .sourceId(entity.getSourceId())
                .contentText(entity.getContentText())
                .vector(entity.getVector())
                .model(entity.getModel())
                .dimensions(entity.getDimensions())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
