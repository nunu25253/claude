package com.buzzanalysis.domain.proposal;

import com.buzzanalysis.domain.preprocessing.ContentFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * AIが生成した投稿企画（Phase10）。1回の生成リクエストにつき複数件（既定20件）が
 * 同一の{@code generationId}でグルーピングされる。
 */
public final class ContentProposal {

    private final UUID id;
    private final UUID generationId;
    private final int sequenceNumber;
    private final String title;
    private final String hookPattern;
    private final String structureSummary;
    private final String callToAction;
    private final String targetAudience;
    private final String genre;
    private final ContentFormat recommendedFormat;
    private final String reasoning;
    private final OffsetDateTime createdAt;

    private ContentProposal(Builder b) {
        this.id = b.id;
        this.generationId = b.generationId;
        this.sequenceNumber = b.sequenceNumber;
        this.title = b.title;
        this.hookPattern = b.hookPattern;
        this.structureSummary = b.structureSummary;
        this.callToAction = b.callToAction;
        this.targetAudience = b.targetAudience;
        this.genre = b.genre;
        this.recommendedFormat = b.recommendedFormat;
        this.reasoning = b.reasoning;
        this.createdAt = b.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public UUID getGenerationId() {
        return generationId;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getHookPattern() {
        return hookPattern;
    }

    public String getStructureSummary() {
        return structureSummary;
    }

    public String getCallToAction() {
        return callToAction;
    }

    public String getTargetAudience() {
        return targetAudience;
    }

    public String getGenre() {
        return genre;
    }

    public ContentFormat getRecommendedFormat() {
        return recommendedFormat;
    }

    public String getReasoning() {
        return reasoning;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private UUID id;
        private UUID generationId;
        private int sequenceNumber;
        private String title;
        private String hookPattern;
        private String structureSummary;
        private String callToAction;
        private String targetAudience;
        private String genre;
        private ContentFormat recommendedFormat;
        private String reasoning;
        private OffsetDateTime createdAt;

        public Builder id(UUID v) {
            this.id = v;
            return this;
        }

        public Builder generationId(UUID v) {
            this.generationId = v;
            return this;
        }

        public Builder sequenceNumber(int v) {
            this.sequenceNumber = v;
            return this;
        }

        public Builder title(String v) {
            this.title = v;
            return this;
        }

        public Builder hookPattern(String v) {
            this.hookPattern = v;
            return this;
        }

        public Builder structureSummary(String v) {
            this.structureSummary = v;
            return this;
        }

        public Builder callToAction(String v) {
            this.callToAction = v;
            return this;
        }

        public Builder targetAudience(String v) {
            this.targetAudience = v;
            return this;
        }

        public Builder genre(String v) {
            this.genre = v;
            return this;
        }

        public Builder recommendedFormat(ContentFormat v) {
            this.recommendedFormat = v;
            return this;
        }

        public Builder reasoning(String v) {
            this.reasoning = v;
            return this;
        }

        public Builder createdAt(OffsetDateTime v) {
            this.createdAt = v;
            return this;
        }

        public ContentProposal build() {
            return new ContentProposal(this);
        }
    }
}
