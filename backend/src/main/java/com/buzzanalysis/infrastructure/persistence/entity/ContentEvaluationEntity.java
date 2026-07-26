package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.infrastructure.persistence.converter.StringListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@code content_evaluations} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "content_evaluations")
public class ContentEvaluationEntity {

    @Id
    private UUID id;

    @Column(name = "proposal_id")
    private UUID proposalId;

    @Column(name = "title", columnDefinition = "TEXT")
    private String title;

    @Column(name = "match_rate_percent")
    private Double matchRatePercent;

    @Column(name = "target_audience_estimate", columnDefinition = "TEXT")
    private String targetAudienceEstimate;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "improvement_suggestions", columnDefinition = "TEXT", nullable = false)
    private List<String> improvementSuggestions;

    @Column(name = "hook_improvement", columnDefinition = "TEXT")
    private String hookImprovement;

    @Column(name = "cta_improvement", columnDefinition = "TEXT")
    private String ctaImprovement;

    @Column(name = "predicted_score", nullable = false)
    private int predictedScore;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected ContentEvaluationEntity() {
    }

    public ContentEvaluationEntity(UUID id, UUID proposalId, String title, Double matchRatePercent,
                                    String targetAudienceEstimate, List<String> improvementSuggestions,
                                    String hookImprovement, String ctaImprovement, int predictedScore,
                                    OffsetDateTime createdAt) {
        this.id = id;
        this.proposalId = proposalId;
        this.title = title;
        this.matchRatePercent = matchRatePercent;
        this.targetAudienceEstimate = targetAudienceEstimate;
        this.improvementSuggestions = improvementSuggestions;
        this.hookImprovement = hookImprovement;
        this.ctaImprovement = ctaImprovement;
        this.predictedScore = predictedScore;
        this.createdAt = createdAt;
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
}
