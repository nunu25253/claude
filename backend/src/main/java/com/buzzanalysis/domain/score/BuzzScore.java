package com.buzzanalysis.domain.score;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 投稿に紐づくバズスコアとその内訳（Strategyごとのスコア）を表す集約。
 */
public class BuzzScore {

    private final UUID id;
    private final UUID postId;
    private final double totalScore;
    private final Map<String, Double> breakdown;
    private final OffsetDateTime calculatedAt;

    public BuzzScore(UUID id, UUID postId, double totalScore, Map<String, Double> breakdown, OffsetDateTime calculatedAt) {
        this.id = id;
        this.postId = postId;
        this.totalScore = totalScore;
        this.breakdown = breakdown;
        this.calculatedAt = calculatedAt;
    }

    public static BuzzScore of(UUID postId, double totalScore, Map<String, Double> breakdown) {
        return new BuzzScore(UUID.randomUUID(), postId, totalScore, breakdown, OffsetDateTime.now());
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
