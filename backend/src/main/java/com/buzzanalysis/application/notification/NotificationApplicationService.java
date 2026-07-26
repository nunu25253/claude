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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * アプリ内通知センターのユースケース。しきい値アラート・週次ダイジェスト等、メールでも通知する
 * イベントについて{@link #notify}経由でアプリ内通知を作成する(各バッチ/ユースケースの
 * MailSenderPort呼び出しと対にして使う)。
 */
@Service
public class NotificationApplicationService {

    /** 一覧取得時に返す最大件数。無制限に蓄積されても画面上は直近分だけで十分なため上限を設ける。 */
    private static final int MAX_LIST_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final SlackNotifierPort slackNotifierPort;

    public NotificationApplicationService(NotificationRepository notificationRepository,
                                           UserSettingsRepository userSettingsRepository,
                                           SlackNotifierPort slackNotifierPort) {
        this.notificationRepository = notificationRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.slackNotifierPort = slackNotifierPort;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> list(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, MAX_LIST_SIZE).stream()
                .map(NotificationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(UUID userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> EntityNotFoundException.of("Notification", notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessRuleViolationException("You are not allowed to modify this notification");
        }
        notification.markRead(OffsetDateTime.now());
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        notificationRepository.markAllReadByUserId(userId);
    }

    /**
     * しきい値アラート・週次ダイジェスト等のバッチ処理から呼び出し、アプリ内通知を1件作成する。
     * ユーザーがSlack Webhook URLを設定済みの場合は、メール到達率の低さを補う代替チャネルとして
     * 同内容をSlackにも送信する(シニアレビュー: 通知チャネルがメールのみ、への対応)。
     */
    @Transactional
    public void notify(UUID userId, NotificationType type, String title, String body, String link) {
        notificationRepository.save(Notification.create(userId, type, title, body, link));

        userSettingsRepository.findByUserId(userId)
                .map(UserSettings::getSlackWebhookUrl)
                .filter(url -> url != null && !url.isBlank())
                .ifPresent(webhookUrl -> slackNotifierPort.sendMessage(webhookUrl, title + "\n" + body));
    }
}
