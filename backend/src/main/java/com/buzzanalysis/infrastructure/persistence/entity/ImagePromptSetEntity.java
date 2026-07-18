package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.domain.imageprompt.ImagePrompt;
import com.buzzanalysis.domain.imageprompt.ImagePromptSourceType;
import com.buzzanalysis.infrastructure.persistence.converter.ImagePromptListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@code image_prompt_sets} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "image_prompt_sets")
public class ImagePromptSetEntity {

    @Id
    private UUID id;

    @Column(name = "source_type", length = 30, nullable = false)
    @Enumerated(EnumType.STRING)
    private ImagePromptSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Convert(converter = ImagePromptListJsonConverter.class)
    @Column(name = "prompts", columnDefinition = "TEXT", nullable = false)
    private List<ImagePrompt> prompts;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ImagePromptSetEntity() {
    }

    public ImagePromptSetEntity(UUID id, ImagePromptSourceType sourceType, UUID sourceId, List<ImagePrompt> prompts,
                                 OffsetDateTime createdAt) {
        this.id = id;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.prompts = prompts;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public ImagePromptSourceType getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public List<ImagePrompt> getPrompts() {
        return prompts;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
