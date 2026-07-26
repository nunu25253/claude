package com.buzzanalysis.application.notification;

import com.buzzanalysis.application.notification.dto.NotificationDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.common.exception.EntityNotFoundException;
import com.buzzanalysis.domain.notification.Notification;
import com.buzzanalysis.domain.notification.NotificationRepository;
import com.buzzanalysis.domain.notification.NotificationType;
import com.buzzanalysis.domain.notification.SlackNotifierPort;
import com.buzzanalysis.domain.settings.UserSettings;
import com.buzzanalysis.domain.settings.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** {@link NotificationApplicationService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class NotificationApplicationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserSettingsRepository userSettingsRepository;
    @Mock
    private SlackNotifierPort slackNotifierPort;

    private NotificationApplicationService service;
    private UUID userId;

    @BeforeEach
    void setUp() {
        service = new NotificationApplicationService(notificationRepository, userSettingsRepository, slackNotifierPort);
        userId = UUID.randomUUID();
    }

    @Test
    void list_returnsNotificationsMappedToDto() {
        Notification n = Notification.create(userId, NotificationType.THRESHOLD_ALERT, "件名", "本文", "/saved");
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, 50)).thenReturn(List.of(n));

        List<NotificationDto> result = service.list(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("件名");
        assertThat(result.get(0).unread()).isTrue();
    }

    @Test
    void unreadCount_delegatesToRepository() {
        when(notificationRepository.countUnreadByUserId(userId)).thenReturn(3L);

        assertThat(service.unreadCount(userId)).isEqualTo(3L);
    }

    @Test
    void markRead_marksNotificationAsRead_whenOwnedByRequestingUser() {
        Notification n = Notification.create(userId, NotificationType.WEEKLY_DIGEST, "件名", "本文", "/saved");
        when(notificationRepository.findById(n.getId())).thenReturn(Optional.of(n));

        service.markRead(userId, n.getId());

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getReadAt()).isNotNull();
    }

    @Test
    void markRead_throwsEntityNotFound_whenNotificationDoesNotExist() {
        UUID missingId = UUID.randomUUID();
        when(notificationRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markRead(userId, missingId)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void markRead_throwsBusinessRuleViolation_whenNotificationBelongsToAnotherUser() {
        UUID otherUserId = UUID.randomUUID();
        Notification n = Notification.create(otherUserId, NotificationType.THRESHOLD_ALERT, "件名", "本文", "/saved");
        when(notificationRepository.findById(n.getId())).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.markRead(userId, n.getId()))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllRead_delegatesToRepository() {
        service.markAllRead(userId);

        verify(notificationRepository).markAllReadByUserId(userId);
    }

    @Test
    void notify_savesNewNotification() {
        service.notify(userId, NotificationType.THRESHOLD_ALERT, "件名", "本文", "/saved");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getType()).isEqualTo(NotificationType.THRESHOLD_ALERT);
        assertThat(captor.getValue().isUnread()).isTrue();
    }

    @Test
    void notify_doesNotCallSlack_whenNoWebhookConfigured() {
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.empty());

        service.notify(userId, NotificationType.THRESHOLD_ALERT, "件名", "本文", "/saved");

        verify(slackNotifierPort, never()).sendMessage(any(), any());
    }

    @Test
    void notify_sendsSlackMessage_whenWebhookConfigured() {
        UserSettings settings = new UserSettings(userId, true, false, false,
                "https://hooks.slack.com/services/test", null, null, null, null);
        when(userSettingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

        service.notify(userId, NotificationType.THRESHOLD_ALERT, "件名", "本文", "/saved");

        verify(slackNotifierPort).sendMessage("https://hooks.slack.com/services/test", "件名\n本文");
    }
}
