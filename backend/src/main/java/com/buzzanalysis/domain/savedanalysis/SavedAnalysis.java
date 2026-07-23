package com.buzzanalysis.domain.savedanalysis;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ユーザーがブックマークした分析結果を表す集約。
 * しきい値アラート(alertThreshold)を設定すると、対象投稿のBuzzScoreがしきい値以上になった時点で
 * 1度だけメール通知される(alertTriggeredAtで既に通知済みかを判定する)。
 */
public class SavedAnalysis {

    private final UUID id;
    private final UUID userId;
    private final UUID postId;
    private String note;
    private final OffsetDateTime createdAt;
    private Double alertThreshold;
    private OffsetDateTime alertTriggeredAt;

    public SavedAnalysis(UUID id, UUID userId, UUID postId, String note, OffsetDateTime createdAt,
                          Double alertThreshold, OffsetDateTime alertTriggeredAt) {
        this.id = id;
        this.userId = userId;
        this.postId = postId;
        this.note = note;
        this.createdAt = createdAt;
        this.alertThreshold = alertThreshold;
        this.alertTriggeredAt = alertTriggeredAt;
    }

    public static SavedAnalysis createNew(UUID userId, UUID postId, String note) {
        return new SavedAnalysis(UUID.randomUUID(), userId, postId, note, OffsetDateTime.now(), null, null);
    }

    /** しきい値を設定(nullで解除)する。しきい値を変更した場合は再度アーム状態に戻す(通知済みフラグをクリア)。 */
    public void setAlertThreshold(Double alertThreshold) {
        this.alertThreshold = alertThreshold;
        this.alertTriggeredAt = null;
    }

    /** しきい値が設定済み・未通知・現在スコアがしきい値以上、の3条件を満たすか判定する。 */
    public boolean shouldTriggerAlert(double currentScore) {
        return alertThreshold != null && alertTriggeredAt == null && currentScore >= alertThreshold;
    }

    public void markAlertTriggered(OffsetDateTime triggeredAt) {
        this.alertTriggeredAt = triggeredAt;
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
