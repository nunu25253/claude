package com.buzzanalysis.application.savedanalysis;

import com.buzzanalysis.application.auth.MailSenderPort;
import com.buzzanalysis.application.notification.NotificationApplicationService;
import com.buzzanalysis.domain.notification.NotificationType;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysis;
import com.buzzanalysis.domain.savedanalysis.SavedAnalysisRepository;
import com.buzzanalysis.domain.score.BuzzScore;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import com.buzzanalysis.domain.user.Role;
import com.buzzanalysis.domain.user.User;
import com.buzzanalysis.domain.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** {@link ThresholdAlertApplicationService} の単体テスト。 */
@ExtendWith(MockitoExtension.class)
class ThresholdAlertApplicationServiceTest {

    @Mock
    private SavedAnalysisRepository savedAnalysisRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MailSenderPort mailSenderPort;
    @Mock
    private NotificationApplicationService notificationApplicationService;

    private ThresholdAlertApplicationService service;
    private UUID userId;
    private UUID postId;
    private Post post;
    private User user;

    @BeforeEach
    void setUp() {
        service = new ThresholdAlertApplicationService(savedAnalysisRepository, postRepository,
                buzzScoreRepository, userRepository, mailSenderPort, notificationApplicationService);

        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        post = new Post(postId, UUID.randomUUID(), Platform.X, "12345", "https://x.com/user/status/12345",
                OffsetDateTime.now(), "user", "caption", List.of(), 100L, 10L, 1000L, 5L, null, null,
                PostType.TEXT, OffsetDateTime.now(), OffsetDateTime.now());
        user = new User(userId, "user@example.com", "hash", "User", Role.USER, true,
                OffsetDateTime.now(), OffsetDateTime.now());
    }

    @Test
    void checkAndSendAlerts_sendsEmailAndMarksTriggered_whenScoreExceedsThreshold() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        saved.setAlertThreshold(80.0);
        when(savedAnalysisRepository.findPendingAlerts()).thenReturn(List.of(saved));
        when(buzzScoreRepository.findByPostId(postId)).thenReturn(Optional.of(
                new BuzzScore(UUID.randomUUID(), postId, 85.0, Map.of(), OffsetDateTime.now())));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(savedAnalysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int triggeredCount = service.checkAndSendAlerts();

        assertThat(triggeredCount).isEqualTo(1);
        verify(mailSenderPort).sendThresholdAlertEmail(anyString(), anyString(), anyDouble(), anyDouble());
        verify(notificationApplicationService).notify(eq(userId), eq(NotificationType.THRESHOLD_ALERT), anyString(), anyString(), anyString());
        ArgumentCaptor<SavedAnalysis> captor = ArgumentCaptor.forClass(SavedAnalysis.class);
        verify(savedAnalysisRepository).save(captor.capture());
        assertThat(captor.getValue().getAlertTriggeredAt()).isNotNull();
    }

    @Test
    void checkAndSendAlerts_sendsNothing_whenScoreBelowThreshold() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        saved.setAlertThreshold(80.0);
        when(savedAnalysisRepository.findPendingAlerts()).thenReturn(List.of(saved));
        when(buzzScoreRepository.findByPostId(postId)).thenReturn(Optional.of(
                new BuzzScore(UUID.randomUUID(), postId, 60.0, Map.of(), OffsetDateTime.now())));

        int triggeredCount = service.checkAndSendAlerts();

        assertThat(triggeredCount).isZero();
        verify(mailSenderPort, never()).sendThresholdAlertEmail(anyString(), anyString(), anyDouble(), anyDouble());
        verify(savedAnalysisRepository, never()).save(any());
    }

    @Test
    void checkAndSendAlerts_skipsSilently_whenPostNoLongerExists() {
        SavedAnalysis saved = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        saved.setAlertThreshold(80.0);
        when(savedAnalysisRepository.findPendingAlerts()).thenReturn(List.of(saved));
        when(buzzScoreRepository.findByPostId(postId)).thenReturn(Optional.of(
                new BuzzScore(UUID.randomUUID(), postId, 90.0, Map.of(), OffsetDateTime.now())));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        int triggeredCount = service.checkAndSendAlerts();

        assertThat(triggeredCount).isZero();
        verify(mailSenderPort, never()).sendThresholdAlertEmail(anyString(), anyString(), anyDouble(), anyDouble());
    }

    @Test
    void checkAndSendAlerts_processesMultipleEntriesIndependently() {
        UUID postId2 = UUID.randomUUID();
        SavedAnalysis saved1 = new SavedAnalysis(UUID.randomUUID(), userId, postId, null, OffsetDateTime.now(), null, null);
        saved1.setAlertThreshold(80.0);
        SavedAnalysis saved2 = new SavedAnalysis(UUID.randomUUID(), userId, postId2, null, OffsetDateTime.now(), null, null);
        saved2.setAlertThreshold(80.0);
        when(savedAnalysisRepository.findPendingAlerts()).thenReturn(List.of(saved1, saved2));
        when(buzzScoreRepository.findByPostId(postId)).thenReturn(Optional.of(
                new BuzzScore(UUID.randomUUID(), postId, 90.0, Map.of(), OffsetDateTime.now())));
        when(buzzScoreRepository.findByPostId(postId2)).thenReturn(Optional.of(
                new BuzzScore(UUID.randomUUID(), postId2, 90.0, Map.of(), OffsetDateTime.now())));
        when(postRepository.findById(any())).thenReturn(Optional.of(post));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(savedAnalysisRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int triggeredCount = service.checkAndSendAlerts();

        assertThat(triggeredCount).isEqualTo(2);
        verify(mailSenderPort, times(2)).sendThresholdAlertEmail(anyString(), anyString(), anyDouble(), anyDouble());
    }
}
