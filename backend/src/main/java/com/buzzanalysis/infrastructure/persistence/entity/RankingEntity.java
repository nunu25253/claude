package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code rankings} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "rankings")
public class RankingEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RankingTypeEnum type;

    @Column(name = "genre")
    private String genre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PlatformEnum platform;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "rank_position", nullable = false)
    private int rankPosition;

    @Column(nullable = false)
    private double score;

    @Column(name = "period_start")
    private OffsetDateTime periodStart;

    @Column(name = "period_end")
    private OffsetDateTime periodEnd;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected RankingEntity() {
    }

    public RankingEntity(UUID id, RankingTypeEnum type, String genre, PlatformEnum platform, UUID postId,
                          int rankPosition, double score, OffsetDateTime periodStart, OffsetDateTime periodEnd,
                          OffsetDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.genre = genre;
        this.platform = platform;
        this.postId = postId;
        this.rankPosition = rankPosition;
        this.score = score;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.createdAt = createdAt;
    }

    public enum RankingTypeEnum {
        TRENDING, WEEKLY, MONTHLY
    }

    public UUID getId() {
        return id;
    }

    public RankingTypeEnum getType() {
        return type;
    }

    public String getGenre() {
        return genre;
    }

    public PlatformEnum getPlatform() {
        return platform;
    }

    public UUID getPostId() {
        return postId;
    }

    public int getRankPosition() {
        return rankPosition;
    }

    public double getScore() {
        return score;
    }

    public OffsetDateTime getPeriodStart() {
        return periodStart;
    }

    public OffsetDateTime getPeriodEnd() {
        return periodEnd;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
