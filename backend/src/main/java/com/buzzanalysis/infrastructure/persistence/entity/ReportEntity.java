package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code reports} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "reports")
public class ReportEntity {

    @Id
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportFormatEnum format;

    @Column(name = "title")
    private String title;

    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "content_size_bytes", nullable = false)
    private long contentSizeBytes;

    @Column(name = "generated_at", nullable = false)
    private OffsetDateTime generatedAt;

    protected ReportEntity() {
    }

    public ReportEntity(UUID id, UUID postId, ReportFormatEnum format, String title, String storageKey,
                         long contentSizeBytes, OffsetDateTime generatedAt) {
        this.id = id;
        this.postId = postId;
        this.format = format;
        this.title = title;
        this.storageKey = storageKey;
        this.contentSizeBytes = contentSizeBytes;
        this.generatedAt = generatedAt;
    }

    public enum ReportFormatEnum {
        PDF, MARKDOWN, HTML
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public ReportFormatEnum getFormat() {
        return format;
    }

    public String getTitle() {
        return title;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public long getContentSizeBytes() {
        return contentSizeBytes;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }
}
