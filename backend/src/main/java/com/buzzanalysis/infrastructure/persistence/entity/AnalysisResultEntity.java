package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code analysis_results} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "analysis_results")
public class AnalysisResultEntity {

    @Id
    private UUID id;

    @Column(name = "post_id", nullable = false, unique = true)
    private UUID postId;

    @Column(name = "why_it_went_viral", columnDefinition = "TEXT")
    private String whyItWentViral;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Column(name = "hook", columnDefinition = "TEXT")
    private String hook;

    @Column(name = "call_to_action", columnDefinition = "TEXT")
    private String callToAction;

    @Column(name = "sentiment_analysis", columnDefinition = "TEXT")
    private String sentimentAnalysis;

    @Column(name = "video_structure_analysis", columnDefinition = "TEXT")
    private String videoStructureAnalysis;

    @Column(name = "carousel_structure_analysis", columnDefinition = "TEXT")
    private String carouselStructureAnalysis;

    @Column(name = "title_analysis", columnDefinition = "TEXT")
    private String titleAnalysis;

    @Column(name = "text_analysis", columnDefinition = "TEXT")
    private String textAnalysis;

    @Column(name = "posting_time_analysis", columnDefinition = "TEXT")
    private String postingTimeAnalysis;

    @Column(name = "hashtag_analysis", columnDefinition = "TEXT")
    private String hashtagAnalysis;

    @Column(name = "improvement_suggestions", columnDefinition = "TEXT")
    private String improvementSuggestions;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected AnalysisResultEntity() {
    }

    public AnalysisResultEntity(UUID id, UUID postId, String whyItWentViral, String targetAudience, String hook,
                                 String callToAction, String sentimentAnalysis, String videoStructureAnalysis,
                                 String carouselStructureAnalysis, String titleAnalysis, String textAnalysis,
                                 String postingTimeAnalysis, String hashtagAnalysis, String improvementSuggestions,
                                 OffsetDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.whyItWentViral = whyItWentViral;
        this.targetAudience = targetAudience;
        this.hook = hook;
        this.callToAction = callToAction;
        this.sentimentAnalysis = sentimentAnalysis;
        this.videoStructureAnalysis = videoStructureAnalysis;
        this.carouselStructureAnalysis = carouselStructureAnalysis;
        this.titleAnalysis = titleAnalysis;
        this.textAnalysis = textAnalysis;
        this.postingTimeAnalysis = postingTimeAnalysis;
        this.hashtagAnalysis = hashtagAnalysis;
        this.improvementSuggestions = improvementSuggestions;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public String getWhyItWentViral() {
        return whyItWentViral;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public String getHook() {
        return hook;
    }

    public String getCallToAction() {
        return callToAction;
    }

    public String getSentimentAnalysis() {
        return sentimentAnalysis;
    }

    public String getVideoStructureAnalysis() {
        return videoStructureAnalysis;
    }

    public String getCarouselStructureAnalysis() {
        return carouselStructureAnalysis;
    }

    public String getTitleAnalysis() {
        return titleAnalysis;
    }

    public String getTextAnalysis() {
        return textAnalysis;
    }

    public String getPostingTimeAnalysis() {
        return postingTimeAnalysis;
    }

    public String getHashtagAnalysis() {
        return hashtagAnalysis;
    }

    public String getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
