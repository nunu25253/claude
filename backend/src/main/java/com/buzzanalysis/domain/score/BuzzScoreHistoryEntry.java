package com.buzzanalysis.domain.score;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 投稿の再分析ごとに追記されるBuzzScoreの履歴1件分。{@link BuzzScore}(最新値)と異なり、
 * 同一投稿について複数件保持することでBuzzScore推移の可視化を可能にする。
 */
public class BuzzScoreHistoryEntry {

    private final UUID id;
    private final UUID postId;
    private final double totalScore;
    private final Map<String, Double> breakdown;
    private final OffsetDateTime calculatedAt;

    public BuzzScoreHistoryEntry(UUID id, UUID postId, double totalScore, Map<String, Double> breakdown,
                                  OffsetDateTime calculatedAt) {
        this.id = id;
        this.postId = postId;
        this.totalScore = totalScore;
        this.breakdown = breakdown;
        this.calculatedAt = calculatedAt;
    }

    public static BuzzScoreHistoryEntry of(UUID postId, double totalScore, Map<String, Double> breakdown,
                                            OffsetDateTime calculatedAt) {
        return new BuzzScoreHistoryEntry(UUID.randomUUID(), postId, totalScore, breakdown, calculatedAt);
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public Map<String, Double> getBreakdown() {
        return breakdown;
    }

    public OffsetDateTime getCalculatedAt() {
        return calculatedAt;
    }
}
