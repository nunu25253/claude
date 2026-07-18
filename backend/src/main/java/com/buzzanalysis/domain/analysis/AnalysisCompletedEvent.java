package com.buzzanalysis.domain.analysis;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 投稿分析（AI分析 + BuzzScore算出）が完了したことを表すドメインイベント（Observerパターンの通知内容）。
 * フレームワーク非依存のPOJOとして定義し、application層で {@code ApplicationEventPublisher} を用いて発行する。
 * Observer（Listener）はレポート生成のトリガーやキャッシュ更新などを行う。
 */
public final class AnalysisCompletedEvent {

    private final UUID postId;
    private final UUID analysisResultId;
    private final double buzzScore;
    private final OffsetDateTime occurredAt;

    public AnalysisCompletedEvent(UUID postId, UUID analysisResultId, double buzzScore) {
        this.postId = postId;
        this.analysisResultId = analysisResultId;
        this.buzzScore = buzzScore;
        this.occurredAt = OffsetDateTime.now();
    }

    public UUID getPostId() {
        return postId;
    }

    public UUID getAnalysisResultId() {
        return analysisResultId;
    }

    public double getBuzzScore() {
        return buzzScore;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }
}
