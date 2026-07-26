package com.buzzanalysis.application.settings.dto;

import com.buzzanalysis.domain.settings.UserSettings;
import jakarta.validation.constraints.Pattern;

/** フロントエンドの{@code NotificationSettings}型に対応するDTO。 */
public record NotificationSettingsDto(boolean emailOnAnalysisComplete, boolean emailWeeklyDigest,
                                       boolean emailTrendingAlert,
                                       @Pattern(regexp = "^$|^https://.+", message = "Slack Webhook URLはhttps://で始まる必要があります")
                                       String slackWebhookUrl) {
    public static NotificationSettingsDto from(UserSettings settings) {
        return new NotificationSettingsDto(settings.isEmailOnAnalysisComplete(), settings.isEmailWeeklyDigest(),
                settings.isEmailTrendingAlert(), settings.getSlackWebhookUrl());
    }
}
