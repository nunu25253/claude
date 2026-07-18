package com.buzzanalysis.domain.savedanalysis;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ユーザーがブックマークした分析結果を表す集約。
 */
public class SavedAnalysis {

    private final UUID id;
    private final UUID userId;
    private final UUID postId;
    private String note;
    private final OffsetDateTime createdAt;

    public SavedAnalysis(UUID id, UUID userId, UUID postId, String note, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.note = note;
        this.createdAt = createdAt;
    }

    public static SavedAnalysis createNew(UUID userId, UUID postId, String note) {
        return new SavedAnalysis(UUID.randomUUID(), userId, postId, note, OffsetDateTime.now());
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
}
