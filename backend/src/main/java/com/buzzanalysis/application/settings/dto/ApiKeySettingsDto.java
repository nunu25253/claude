package com.buzzanalysis.application.settings.dto;

import com.buzzanalysis.domain.settings.UserSettings;

import java.time.OffsetDateTime;

/** フロントエンドの{@code ApiKeySettings}型に対応するDTO。APIキー未発行時は{@code apiKey}がnull。 */
public record ApiKeySettingsDto(String apiKey, OffsetDateTime createdAt) {
    public static ApiKeySettingsDto from(UserSettings settings) {
        return new ApiKeySettingsDto(settings.getApiKey(), settings.getApiKeyCreatedAt());
    }
}
