package com.buzzanalysis.domain.ranking;

import com.buzzanalysis.domain.platform.Platform;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ランキングの1エントリ（急上昇/週間/月間/ジャンル別/SNS別）を表す集約。
 */
public class Ranking {

    private final UUID id;
    private final RankingType type;
    private final String genre;
    private final Platform platform;
    private final UUID postId;
    private final int rankPosition;
    private final double score;
    private final OffsetDateTime periodStart;
    private final OffsetDateTime periodEnd;
    private final OffsetDateTime createdAt;

    public Ranking(UUID id, RankingType type, String genre, Platform platform, UUID postId, int rankPosition,
                   double score, OffsetDateTime periodStart, OffsetDateTime periodEnd, OffsetDateTime createdAt) {
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

    public static Ranking createNew(RankingType type, String genre, Platform platform, UUID postId,
                                     int rankPosition, double score, OffsetDateTime periodStart, OffsetDateTime periodEnd) {
        return new Ranking(UUID.randomUUID(), type, genre, platform, postId, rankPosition, score,
                periodStart, periodEnd, OffsetDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public RankingType getType() {
        return type;
    }

    public String getGenre() {
        return genre;
    }

    public Platform getPlatform() {
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
