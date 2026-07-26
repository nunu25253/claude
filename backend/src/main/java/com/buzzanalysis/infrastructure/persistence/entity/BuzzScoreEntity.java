package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.infrastructure.persistence.converter.StringDoubleMapJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/** {@code buzz_scores} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "buzz_scores")
public class BuzzScoreEntity {

    @Id
    private UUID id;

    @Column(name = "post_id", nullable = false, unique = true)
    private UUID postId;

    @Column(name = "total_score", nullable = false)
    private double totalScore;

    @Convert(converter = StringDoubleMapJsonConverter.class)
    @Column(name = "breakdown", nullable = false, columnDefinition = "TEXT")
    private Map<String, Double> breakdown;

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt;

    protected BuzzScoreEntity() {
    }

    public BuzzScoreEntity(UUID id, UUID postId, double totalScore, Map<String, Double> breakdown, OffsetDateTime calculatedAt) {
        this.id = id;
        this.postId = postId;
        this.totalScore = totalScore;
        this.breakdown = breakdown;
        this.calculatedAt = calculatedAt;
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
