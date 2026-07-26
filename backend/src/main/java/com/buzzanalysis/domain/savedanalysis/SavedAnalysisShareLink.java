package com.buzzanalysis.domain.savedanalysis;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 保存済み分析を未ログインの外部クライアントに閲覧専用で共有するためのリンク。
 * idそのものを公開トークンとして扱う(推測不可能なUUIDであることをアクセス制御とする、
 * {@code /api/v1/reports/files/**}の署名付きURLと同じ考え方)。
 */
public class SavedAnalysisShareLink {

    private final UUID id;
    private final UUID savedAnalysisId;
    private final UUID createdByUserId;
    private final OffsetDateTime createdAt;
    private OffsetDateTime revokedAt;

    public SavedAnalysisShareLink(UUID id, UUID savedAnalysisId, UUID createdByUserId, OffsetDateTime createdAt,
                                   OffsetDateTime revokedAt) {
        this.id = id;
        this.savedAnalysisId = savedAnalysisId;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
    }

    public static SavedAnalysisShareLink create(UUID savedAnalysisId, UUID createdByUserId) {
        return new SavedAnalysisShareLink(UUID.randomUUID(), savedAnalysisId, createdByUserId,
                OffsetDateTime.now(), null);
    }

    public void revoke(OffsetDateTime now) {
        revokedAt = now;
    }

    public boolean isActive() {
        return revokedAt == null;
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
