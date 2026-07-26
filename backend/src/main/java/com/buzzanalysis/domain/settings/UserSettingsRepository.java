package com.buzzanalysis.domain.settings;

import java.util.Optional;
import java.util.UUID;

/** {@link UserSettings} の永続化を抽象化するリポジトリ（Repositoryパターン）。 */
public interface UserSettingsRepository {

    UserSettings save(UserSettings settings);

    Optional<UserSettings> findByUserId(UUID userId);
}
