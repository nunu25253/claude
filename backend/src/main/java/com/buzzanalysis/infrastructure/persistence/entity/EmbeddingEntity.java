package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.infrastructure.persistence.type.VectorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code embeddings} テーブルに対応するJPAエンティティ。{@code vector} 列はpgvectorの {@code vector} 型。 */
@Entity
@Table(name = "embeddings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "target"})
})
public class EmbeddingEntity {

    @Id
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmbeddingTargetEnum target;

    @Type(VectorType.class)
    @Column(name = "vector", columnDefinition = "vector(1536)", nullable = false)
    private float[] vector;

    @Column(name = "source_text", nullable = false, columnDefinition = "TEXT")
    private String sourceText;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(nullable = false)
    private int dimensions;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    protected EmbeddingEntity() {
    }

    public EmbeddingEntity(UUID id, UUID postId, EmbeddingTargetEnum target, float[] vector, String sourceText,
                            String model, int dimensions, OffsetDateTime generatedAt) {
        this.id = id;
        this.postId = postId;
        this.target = target;
        this.vector = vector;
        this.sourceText = sourceText;
        this.model = model;
        this.dimensions = dimensions;
        this.generatedAt = generatedAt;
    }

    /** 既存レコードを新しいベクトル/元テキストで上書きする（同一 post_id + target への再生成時に使用）。 */
    public void update(float[] vector, String sourceText, String model, int dimensions, OffsetDateTime generatedAt) {
        this.vector = vector;
        this.sourceText = sourceText;
        this.model = model;
        this.dimensions = dimensions;
        this.generatedAt = generatedAt;
    }

    public enum EmbeddingTargetEnum {
        TITLE, BODY, HASHTAGS, COMMENT_SUMMARY
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public EmbeddingTargetEnum getTarget() {
        return target;
    }

    public float[] getVector() {
        return vector;
    }

    public String getSourceText() {
        return sourceText;
    }

    public String getModel() {
        return model;
    }

    public int getDimensions() {
        return dimensions;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }
}
