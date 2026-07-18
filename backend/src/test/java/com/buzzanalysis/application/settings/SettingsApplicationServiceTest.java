package com.buzzanalysis.application.settings;

import com.buzzanalysis.application.settings.dto.ApiKeySettingsDto;
import com.buzzanalysis.application.settings.dto.NotificationSettingsDto;
import com.buzzanalysis.application.settings.dto.ProfileSettingsDto;
import com.buzzanalysis.application.settings.dto.UpdateProfileRequest;
import com.buzzanalysis.domain.settings.UserSettings;
import com.buzzanalysis.domain.settings.UserSettingsRepository;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettingsApplicationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSettingsRepository userSettingsRepository;

    private SettingsApplicationService service;
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        service = new SettingsApplicationService(userRepository, userSettingsRepository);
        userId = UUID.randomUUID();
        user = new User(userId, "user@example.com", "hashed", "元の名前", Role.USER,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void updateProfile_updatesDisplayName_butNotEmail() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProfileSettingsDto result = service.updateProfile(userId, new UpdateProfileRequest("新しい名前", "ignored@example.com"));

        assertThat(result.displayName()).isEqualTo("新しい名前");
        assertThat(result.email()).isEqualTo("user@example.com");
    }

    @Test
    void getNotifications_createsDefaultSettings_whenNoneExistYet() {
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationSettingsDto result = service.getNotifications(userId);

        assertThat(result.emailOnAnalysisComplete()).isTrue();
        assertThat(result.emailWeeklyDigest()).isFalse();
        assertThat(result.emailTrendingAlert()).isFalse();
    }

    @Test
    void updateNotifications_persistsNewFlags() {
        UserSettings existing = UserSettings.createDefault(userId);
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(userSettingsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationSettingsDto result = service.updateNotifications(userId,
                new NotificationSettingsDto(false, true, true));

        assertThat(result.emailOnAnalysisComplete()).isFalse();
        assertThat(result.emailWeeklyDigest()).isTrue();
        assertThat(result.emailTrendingAlert()).isTrue();
    }

    @Test
    void regenerateApiKey_producesNonBlankKeyWithBzPrefix() {
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApiKeySettingsDto result = service.regenerateApiKey(userId);

        assertThat(result.apiKey()).startsWith("bz_");
        assertThat(result.createdAt()).isNotNull();
    }

    @Test
    void regenerateApiKey_producesDifferentKeys_onSuccessiveCalls() {
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSettingsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        String first = service.regenerateApiKey(userId).apiKey();
        when(userSettingsRepository.findByUserId(userId))
                .thenReturn(Optional.of(UserSettings.createDefault(userId)));
        String second = service.regenerateApiKey(userId).apiKey();

        assertThat(first).isNotEqualTo(second);
    }
}
