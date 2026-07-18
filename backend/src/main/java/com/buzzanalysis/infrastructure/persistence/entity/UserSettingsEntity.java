package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code user_settings} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "user_settings")
public class UserSettingsEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "email_on_analysis_complete", nullable = false)
    private boolean emailOnAnalysisComplete;

    @Column(name = "email_weekly_digest", nullable = false)
    private boolean emailWeeklyDigest;

    @Column(name = "email_trending_alert", nullable = false)
    private boolean emailTrendingAlert;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "api_key_created_at")
    private OffsetDateTime apiKeyCreatedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected UserSettingsEntity() {
    }

    public UserSettingsEntity(UUID userId, boolean emailOnAnalysisComplete, boolean emailWeeklyDigest,
                               boolean emailTrendingAlert, String apiKey, OffsetDateTime apiKeyCreatedAt,
                               OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.userId = userId;
        this.emailOnAnalysisComplete = emailOnAnalysisComplete;
        this.emailWeeklyDigest = emailWeeklyDigest;
        this.emailTrendingAlert = emailTrendingAlert;
        this.apiKey = apiKey;
        this.apiKeyCreatedAt = apiKeyCreatedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public boolean isEmailOnAnalysisComplete() {
        return emailOnAnalysisComplete;
    }

    public boolean isEmailWeeklyDigest() {
        return emailWeeklyDigest;
    }

    public boolean isEmailTrendingAlert() {
        return emailTrendingAlert;
    }

    public String getApiKey() {
        return apiKey;
    }

    public OffsetDateTime getApiKeyCreatedAt() {
        return apiKeyCreatedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
