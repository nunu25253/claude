package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.domain.preprocessing.ContentFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code content_proposals} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "content_proposals")
public class ContentProposalEntity {

    @Id
    private UUID id;

    @Column(name = "generation_id", nullable = false)
    private UUID generationId;

    @Column(name = "sequence_number", nullable = false)
    private int sequenceNumber;

    @Column(name = "title", columnDefinition = "TEXT", nullable = false)
    private String title;

    @Column(name = "hook_pattern", columnDefinition = "TEXT")
    private String hookPattern;

    @Column(name = "structure_summary", columnDefinition = "TEXT")
    private String structureSummary;

    @Column(name = "call_to_action", columnDefinition = "TEXT")
    private String callToAction;

    @Column(name = "target_audience", columnDefinition = "TEXT")
    private String targetAudience;

    @Column(name = "genre", length = 100)
    private String genre;

    @Column(name = "recommended_format", length = 30)
    @Enumerated(EnumType.STRING)
    private ContentFormat recommendedFormat;

    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ContentProposalEntity() {
    }

    public ContentProposalEntity(UUID id, UUID generationId, int sequenceNumber, String title, String hookPattern,
                                  String structureSummary, String callToAction, String targetAudience, String genre,
                                  ContentFormat recommendedFormat,
                                  String reasoning, OffsetDateTime createdAt) {
        this.id = id;
        this.generationId = generationId;
        this.sequenceNumber = sequenceNumber;
        this.title = title;
        this.hookPattern = hookPattern;
        this.structureSummary = structureSummary;
        this.callToAction = callToAction;
        this.targetAudience = targetAudience;
        this.genre = genre;
        this.recommendedFormat = recommendedFormat;
        this.reasoning = reasoning;
        this.createdAt = createdAt;
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

    public com.buzzanalysis.domain.preprocessing.ContentFormat getRecommendedFormat() {
        return recommendedFormat;
    }

    public String getReasoning() {
        return reasoning;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
