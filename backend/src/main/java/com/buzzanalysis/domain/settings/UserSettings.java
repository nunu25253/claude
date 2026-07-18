package com.buzzanalysis.domain.settings;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * ユーザー単位の通知設定・APIキーを保持する集約。プロフィール（表示名/メール）は{@code User}集約が
 * 既に保持しているため、ここでは扱わない（既存集約の重複を避ける）。
 */
public final class UserSettings {

    private final UUID userId;
    private boolean emailOnAnalysisComplete;
    private boolean emailWeeklyDigest;
    private boolean emailTrendingAlert;
    private String apiKey;
    private OffsetDateTime apiKeyCreatedAt;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UserSettings(UUID userId, boolean emailOnAnalysisComplete, boolean emailWeeklyDigest,
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

    /** 初回アクセス時の既定設定。分析完了通知のみON、週次ダイジェスト/トレンドアラートはOFF。 */
    public static UserSettings createDefault(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new UserSettings(userId, true, false, false, null, null, now, now);
    }

    public void updateNotifications(boolean emailOnAnalysisComplete, boolean emailWeeklyDigest,
                                     boolean emailTrendingAlert) {
        this.emailOnAnalysisComplete = emailOnAnalysisComplete;
        this.emailWeeklyDigest = emailWeeklyDigest;
        this.emailTrendingAlert = emailTrendingAlert;
        this.updatedAt = OffsetDateTime.now();
    }

    public void regenerateApiKey(String newApiKey) {
        this.apiKey = newApiKey;
        this.apiKeyCreatedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
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
