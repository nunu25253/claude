package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.settings.UserSettings;
import com.buzzanalysis.infrastructure.persistence.entity.UserSettingsEntity;
import org.springframework.stereotype.Component;

/** {@link UserSettings}（ドメイン）と {@link UserSettingsEntity}（JPA）の相互変換を行う。 */
@Component
public class UserSettingsMapper {

    public UserSettingsEntity toEntity(UserSettings s) {
        return new UserSettingsEntity(s.getUserId(), s.isEmailOnAnalysisComplete(), s.isEmailWeeklyDigest(),
                s.isEmailTrendingAlert(), s.getApiKey(), s.getApiKeyCreatedAt(), s.getCreatedAt(), s.getUpdatedAt());
    }

    public UserSettings toDomain(UserSettingsEntity entity) {
        return new UserSettings(entity.getUserId(), entity.isEmailOnAnalysisComplete(),
                entity.isEmailWeeklyDigest(), entity.isEmailTrendingAlert(), entity.getApiKey(),
                entity.getApiKeyCreatedAt(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
