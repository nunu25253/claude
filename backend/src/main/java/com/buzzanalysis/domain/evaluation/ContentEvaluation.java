package com.buzzanalysis.domain.evaluation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AIによる投稿内容（台本/カルーセル等）の評価結果（Phase14）。{@code proposalId}が指定された
 * 場合のみ元企画との一致率(matchRatePercent)が算出される。未指定時はnull（0%と混同しない。
 * Phase1以来の「未計測はnull」原則を踏襲）。
 */
public final class ContentEvaluation {

    private final UUID id;
    private final UUID proposalId;
    private final String title;
    private final Double matchRatePercent;
    private final String targetAudienceEstimate;
    private final List<String> improvementSuggestions;
    private final String hookImprovement;
    private final String ctaImprovement;
    private final int predictedScore;
    private final OffsetDateTime createdAt;

    private ContentEvaluation(Builder b) {
        this.id = b.id;
        this.proposalId = b.proposalId;
        this.title = b.title;
        this.matchRatePercent = b.matchRatePercent;
        this.targetAudienceEstimate = b.targetAudienceEstimate;
        this.improvementSuggestions = b.improvementSuggestions == null ? List.of() : b.improvementSuggestions;
        this.hookImprovement = b.hookImprovement;
        this.ctaImprovement = b.ctaImprovement;
        this.predictedScore = b.predictedScore;
        this.createdAt = b.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getProposalId() {
        return proposalId;
    }

    public String getTitle() {
        return title;
    }

    public Double getMatchRatePercent() {
        return matchRatePercent;
    }

    public String getTargetAudienceEstimate() {
        return targetAudienceEstimate;
    }

    public List<String> getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public String getHookImprovement() {
        return hookImprovement;
    }

    public String getCtaImprovement() {
        return ctaImprovement;
    }

    public int getPredictedScore() {
        return predictedScore;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private UUID id;
        private UUID proposalId;
        private String title;
        private Double matchRatePercent;
        private String targetAudienceEstimate;
        private List<String> improvementSuggestions;
        private String hookImprovement;
        private String ctaImprovement;
        private int predictedScore;
        private OffsetDateTime createdAt;

        public Builder id(UUID v) {
            this.id = v;
            return this;
        }

        public Builder proposalId(UUID v) {
            this.proposalId = v;
            return this;
        }

        public Builder title(String v) {
            this.title = v;
            return this;
        }

        public Builder matchRatePercent(Double v) {
            this.matchRatePercent = v;
            return this;
        }

        public Builder targetAudienceEstimate(String v) {
            this.targetAudienceEstimate = v;
            return this;
        }

        public Builder improvementSuggestions(List<String> v) {
            this.improvementSuggestions = v;
            return this;
        }

        public Builder hookImprovement(String v) {
            this.hookImprovement = v;
            return this;
        }

        public Builder ctaImprovement(String v) {
            this.ctaImprovement = v;
            return this;
        }

        public Builder predictedScore(int v) {
            this.predictedScore = v;
            return this;
        }

        public Builder createdAt(OffsetDateTime v) {
            this.createdAt = v;
            return this;
        }

        public ContentEvaluation build() {
            return new ContentEvaluation(this);
        }
    }
}
