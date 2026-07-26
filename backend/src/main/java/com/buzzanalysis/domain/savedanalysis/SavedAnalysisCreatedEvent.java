package com.buzzanalysis.domain.savedanalysis;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 保存済み分析(ブックマーク)が作成されたことを表すドメインイベント(Observerパターンの通知内容)。
 * {@link com.buzzanalysis.application.event.SavedAnalysisRagIndexingListener}が購読し、
 * 当該投稿のAI分析結果をRAG索引へ自動登録する(戦略監査レポート: RAGアシスタント機能化)。
 */
public final class SavedAnalysisCreatedEvent {

    private final UUID savedAnalysisId;
    private final UUID userId;
    private final UUID postId;
    private final OffsetDateTime occurredAt;

    public SavedAnalysisCreatedEvent(UUID savedAnalysisId, UUID userId, UUID postId) {
        this.savedAnalysisId = savedAnalysisId;
        this.userId = userId;
        this.postId = postId;
        this.occurredAt = OffsetDateTime.now();
    }

    public UUID getSavedAnalysisId() {
        return savedAnalysisId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getPostId() {
        return postId;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
