package com.buzzanalysis.application.settings.dto;

import com.buzzanalysis.domain.settings.UserSettings;

/** フロントエンドの{@code NotificationSettings}型に対応するDTO。 */
public record NotificationSettingsDto(boolean emailOnAnalysisComplete, boolean emailWeeklyDigest,
                                       boolean emailTrendingAlert) {
    public static NotificationSettingsDto from(UserSettings settings) {
        return new NotificationSettingsDto(settings.isEmailOnAnalysisComplete(), settings.isEmailWeeklyDigest(),
                settings.isEmailTrendingAlert());
    }
}
