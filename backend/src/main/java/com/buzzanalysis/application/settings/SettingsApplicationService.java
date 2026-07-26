package com.buzzanalysis.application.settings;

import com.buzzanalysis.application.settings.dto.ApiKeySettingsDto;
import com.buzzanalysis.application.settings.dto.NotificationSettingsDto;
import com.buzzanalysis.application.settings.dto.ProfileSettingsDto;
import com.buzzanalysis.application.settings.dto.UpdateProfileRequest;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.settings.UserSettings;
import com.buzzanalysis.domain.settings.UserSettingsRepository;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

/**
 * 「ユーザー設定」ユースケース。プロフィール（表示名）は既存の{@code User}集約を再利用し、
 * 通知設定・APIキーは新規{@link UserSettings}集約で管理する。
 */
@Service
public class SettingsApplicationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    public SettingsApplicationService(UserRepository userRepository, UserSettingsRepository userSettingsRepository) {
        this.userRepository = userRepository;
        this.userSettingsRepository = userSettingsRepository;
    }

    @Transactional(readOnly = true)
    public ProfileSettingsDto getProfile(UUID userId) {
        return ProfileSettingsDto.from(findUser(userId));
    }

    @Transactional
    public ProfileSettingsDto updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUser(userId);
        user.updateDisplayName(request.displayName());
        return ProfileSettingsDto.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public NotificationSettingsDto getNotifications(UUID userId) {
        return NotificationSettingsDto.from(findOrCreateSettings(userId));
    }

    @Transactional
    public NotificationSettingsDto updateNotifications(UUID userId, NotificationSettingsDto request) {
        UserSettings settings = findOrCreateSettings(userId);
        settings.updateNotifications(request.emailOnAnalysisComplete(), request.emailWeeklyDigest(),
                request.emailTrendingAlert(), request.slackWebhookUrl());
        return NotificationSettingsDto.from(userSettingsRepository.save(settings));
    }

    @Transactional(readOnly = true)
    public ApiKeySettingsDto getApiKey(UUID userId) {
        return ApiKeySettingsDto.from(findOrCreateSettings(userId));
    }

    @Transactional
    public ApiKeySettingsDto regenerateApiKey(UUID userId) {
        UserSettings settings = findOrCreateSettings(userId);
        settings.regenerateApiKey(generateApiKey());
        return ApiKeySettingsDto.from(userSettingsRepository.save(settings));
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId).orElseThrow(() -> EntityNotFoundException.of("User", userId));
    }

    private UserSettings findOrCreateSettings(UUID userId) {
        return userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> userSettingsRepository.save(UserSettings.createDefault(userId)));
    }

    private String generateApiKey() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return "bz_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
