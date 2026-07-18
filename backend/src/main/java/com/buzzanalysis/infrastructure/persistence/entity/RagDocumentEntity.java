package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.domain.rag.RagSourceType;
import com.buzzanalysis.infrastructure.persistence.type.VectorType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code rag_documents} テーブルに対応するJPAエンティティ。{@code vector} 列はpgvectorの {@code vector} 型。 */
@Entity
@Table(name = "rag_documents")
public class RagDocumentEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private RagSourceType sourceType;

    @Column(name = "source_id")
    private UUID sourceId;

    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    private String contentText;

    @Type(VectorType.class)
    @Column(name = "vector", columnDefinition = "vector(1536)", nullable = false)
    private float[] vector;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private int dimensions;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RagDocumentEntity() {
    }

    public RagDocumentEntity(UUID id, RagSourceType sourceType, UUID sourceId, String contentText, float[] vector,
                              String model, int dimensions, OffsetDateTime createdAt) {
        this.id = id;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.contentText = contentText;
        this.vector = vector;
        this.model = model;
        this.dimensions = dimensions;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public RagSourceType getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getContentText() {
        return contentText;
    }

    public float[] getVector() {
        return vector;
    }

    public String getModel() {
        return model;
    }

    public int getDimensions() {
        return dimensions;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
