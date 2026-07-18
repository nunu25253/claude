package com.buzzanalysis.application.settings.dto;

import com.buzzanalysis.domain.user.User;

/** フロントエンドの{@code ProfileSettings}型に対応するDTO。 */
public record ProfileSettingsDto(String displayName, String email) {
    public static ProfileSettingsDto from(User user) {
        return new ProfileSettingsDto(user.getDisplayName(), user.getEmail());
    }
}
