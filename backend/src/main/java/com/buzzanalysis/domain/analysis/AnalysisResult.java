package com.buzzanalysis.domain.analysis;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * AIによる投稿分析結果を表す集約。項目数が多く組み立てが複雑なため、
 * 生成には {@link Builder}（Builderパターン）の使用を必須とする（コンストラクタはpackage外に公開しない）。
 */
public final class AnalysisResult {

    private final UUID id;
    private final UUID postId;
    private final String whyItWentViral;
    private final String targetAudience;
    private final String hook;
    private final String callToAction;
    private final String sentimentAnalysis;
    private final String videoStructureAnalysis;
    private final String carouselStructureAnalysis;
    private final String titleAnalysis;
    private final String textAnalysis;
    private final String postingTimeAnalysis;
    private final String hashtagAnalysis;
    private final String improvementSuggestions;
    private final OffsetDateTime createdAt;

    private AnalysisResult(Builder b) {
        this.id = b.id != null ? b.id : UUID.randomUUID();
        this.postId = Objects.requireNonNull(b.postId, "postId must not be null");
        this.whyItWentViral = b.whyItWentViral;
        this.targetAudience = b.targetAudience;
        this.hook = b.hook;
        this.callToAction = b.callToAction;
        this.sentimentAnalysis = b.sentimentAnalysis;
        this.videoStructureAnalysis = b.videoStructureAnalysis;
        this.carouselStructureAnalysis = b.carouselStructureAnalysis;
        this.titleAnalysis = b.titleAnalysis;
        this.textAnalysis = b.textAnalysis;
        this.postingTimeAnalysis = b.postingTimeAnalysis;
        this.hashtagAnalysis = b.hashtagAnalysis;
        this.improvementSuggestions = b.improvementSuggestions;
        this.createdAt = b.createdAt != null ? b.createdAt : OffsetDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** AnalysisResultを段階的に組み立てるBuilder。 */
    public static final class Builder {
        private UUID id;
        private UUID postId;
        private String whyItWentViral;
        private String targetAudience;
        private String hook;
        private String callToAction;
        private String sentimentAnalysis;
        private String videoStructureAnalysis;
        private String carouselStructureAnalysis;
        private String titleAnalysis;
        private String textAnalysis;
        private String postingTimeAnalysis;
        private String hashtagAnalysis;
        private String improvementSuggestions;
        private OffsetDateTime createdAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder postId(UUID postId) {
            this.postId = postId;
            return this;
        }

        public Builder whyItWentViral(String v) {
            this.whyItWentViral = v;
            return this;
        }

        public Builder targetAudience(String v) {
            this.targetAudience = v;
            return this;
        }

        public Builder hook(String v) {
            this.hook = v;
            return this;
        }

        public Builder callToAction(String v) {
            this.callToAction = v;
            return this;
        }

        public Builder sentimentAnalysis(String v) {
            this.sentimentAnalysis = v;
            return this;
        }

        public Builder videoStructureAnalysis(String v) {
            this.videoStructureAnalysis = v;
            return this;
        }

        public Builder carouselStructureAnalysis(String v) {
            this.carouselStructureAnalysis = v;
            return this;
        }

        public Builder titleAnalysis(String v) {
            this.titleAnalysis = v;
            return this;
        }

        public Builder textAnalysis(String v) {
            this.textAnalysis = v;
            return this;
        }

        public Builder postingTimeAnalysis(String v) {
            this.postingTimeAnalysis = v;
            return this;
        }

        public Builder hashtagAnalysis(String v) {
            this.hashtagAnalysis = v;
            return this;
        }

        public Builder improvementSuggestions(String v) {
            this.improvementSuggestions = v;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AnalysisResult build() {
            return new AnalysisResult(this);
        }
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
