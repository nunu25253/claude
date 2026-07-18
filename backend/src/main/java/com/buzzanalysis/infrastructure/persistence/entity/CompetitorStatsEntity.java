package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.infrastructure.persistence.converter.IntegerDoubleMapJsonConverter;
import com.buzzanalysis.infrastructure.persistence.converter.UuidListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** {@code competitor_stats} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "competitor_stats")
public class CompetitorStatsEntity {

    @Id
    private UUID id;

    @Column(name = "social_account_id", nullable = false, unique = true)
    private UUID socialAccountId;

    @Column(name = "average_like_count", nullable = false)
    private double averageLikeCount;

    @Column(name = "average_comment_count", nullable = false)
    private double averageCommentCount;

    @Column(name = "posting_frequency_per_week", nullable = false)
    private double postingFrequencyPerWeek;

    @Lob
    @Convert(converter = IntegerDoubleMapJsonConverter.class)
    @Column(name = "posting_time_distribution")
    private Map<Integer, Double> postingTimeDistribution;

    @Column(name = "average_video_duration_seconds")
    private Double averageVideoDurationSeconds;

    @Column(name = "average_caption_length", nullable = false)
    private double averageCaptionLength;

    @Lob
    @Convert(converter = UuidListJsonConverter.class)
    @Column(name = "top_performing_post_ids")
    private List<UUID> topPerformingPostIds;

    @Column(name = "calculated_at", nullable = false)
    private OffsetDateTime calculatedAt;

    protected CompetitorStatsEntity() {
    }

    public CompetitorStatsEntity(UUID id, UUID socialAccountId, double averageLikeCount, double averageCommentCount,
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
