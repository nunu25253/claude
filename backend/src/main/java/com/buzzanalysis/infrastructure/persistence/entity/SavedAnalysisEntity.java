package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code saved_analyses} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "saved_analyses")
public class SavedAnalysisEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "note")
    private String note;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "alert_threshold")
    private Double alertThreshold;

    @Column(name = "alert_triggered_at")
    private OffsetDateTime alertTriggeredAt;

    protected SavedAnalysisEntity() {
    }

    public SavedAnalysisEntity(UUID id, UUID userId, UUID postId, String note, OffsetDateTime createdAt,
                                Double alertThreshold, OffsetDateTime alertTriggeredAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.note = note;
        this.createdAt = createdAt;
        this.alertThreshold = alertThreshold;
        this.alertTriggeredAt = alertTriggeredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPostId() {
        return postId;
    }

    public String getNote() {
        return note;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public Double getAlertThreshold() {
        return alertThreshold;
    }

    public OffsetDateTime getAlertTriggeredAt() {
        return alertTriggeredAt;
    }
}
