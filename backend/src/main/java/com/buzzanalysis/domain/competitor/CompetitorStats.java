package com.buzzanalysis.domain.competitor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * アカウント単位の集計統計を表す集約。競合分析APIのレスポンス元データとなる。
 */
public class CompetitorStats {

    private final UUID id;
    private final UUID socialAccountId;
    private final double averageLikeCount;
    private final double averageCommentCount;
    private final double postingFrequencyPerWeek;
    /** 時間帯(0-23) -> 投稿割合 */
    private final Map<Integer, Double> postingTimeDistribution;
    private final Double averageVideoDurationSeconds;
    private final double averageCaptionLength;
    /** バズった投稿の上位ランキング（投稿ID一覧、スコア降順） */
    private final List<UUID> topPerformingPostIds;
    private final OffsetDateTime calculatedAt;

    public CompetitorStats(UUID id, UUID socialAccountId, double averageLikeCount, double averageCommentCount,
                            double postingFrequencyPerWeek, Map<Integer, Double> postingTimeDistribution,
                            Double averageVideoDurationSeconds, double averageCaptionLength,
                            List<UUID> topPerformingPostIds, OffsetDateTime calculatedAt) {
        this.id = id;
        this.socialAccountId = socialAccountId;
        this.averageLikeCount = averageLikeCount;
        this.averageCommentCount = averageCommentCount;
        this.postingFrequencyPerWeek = postingFrequencyPerWeek;
        this.postingTimeDistribution = postingTimeDistribution;
        this.averageVideoDurationSeconds = averageVideoDurationSeconds;
        this.averageCaptionLength = averageCaptionLength;
        this.topPerformingPostIds = topPerformingPostIds;
        this.calculatedAt = calculatedAt;
    }

    public static CompetitorStats calculate(UUID socialAccountId, double avgLikes, double avgComments,
                                             double postingFrequencyPerWeek, Map<Integer, Double> timeDistribution,
                                             Double avgVideoDuration, double avgCaptionLength,
                                             List<UUID> topPerformingPostIds) {
        return new CompetitorStats(UUID.randomUUID(), socialAccountId, avgLikes, avgComments,
                postingFrequencyPerWeek, timeDistribution, avgVideoDuration, avgCaptionLength,
                topPerformingPostIds, OffsetDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getSocialAccountId() {
        return socialAccountId;
    }

    public double getAverageLikeCount() {
        return averageLikeCount;
    }

    public double getAverageCommentCount() {
        return averageCommentCount;
    }

    public double getPostingFrequencyPerWeek() {
        return postingFrequencyPerWeek;
    }

    public Map<Integer, Double> getPostingTimeDistribution() {
        return postingTimeDistribution;
    }

    public Double getAverageVideoDurationSeconds() {
        return averageVideoDurationSeconds;
    }

    public double getAverageCaptionLength() {
        return averageCaptionLength;
    }

    public List<UUID> getTopPerformingPostIds() {
        return topPerformingPostIds;
    }

    public OffsetDateTime getCalculatedAt() {
        return calculatedAt;
    }
}
