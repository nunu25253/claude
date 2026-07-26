package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code saved_analysis_share_links} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "saved_analysis_share_links")
public class SavedAnalysisShareLinkEntity {

    @Id
    private UUID id;

    @Column(name = "saved_analysis_id", nullable = false)
    private UUID savedAnalysisId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "revoked_at")
    private OffsetDateTime revokedAt;

    protected SavedAnalysisShareLinkEntity() {
    }

    public SavedAnalysisShareLinkEntity(UUID id, UUID savedAnalysisId, UUID createdByUserId,
                                         OffsetDateTime createdAt, OffsetDateTime revokedAt) {
        this.id = id;
        this.savedAnalysisId = savedAnalysisId;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSavedAnalysisId() {
        return savedAnalysisId;
    }

    public UUID getCreatedByUserId() {
        return createdByUserId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getRevokedAt() {
        return revokedAt;
    }
}
