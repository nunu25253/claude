package com.buzzanalysis.domain.notification;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * アプリ内通知1件分を表す集約。しきい値アラート・週次ダイジェスト等、メールでも通知する
 * イベントについて、メール到達率・開封率の低さを補う経路として同時に作成する
 * (シニアレビュー: 「通知がメールのみでリテンションループが脆弱」への対応)。
 */
public class Notification {

    private final UUID id;
    private final UUID userId;
    private final NotificationType type;
    private final String title;
    private final String body;
    private final String link;
    private final OffsetDateTime createdAt;
    private OffsetDateTime readAt;

    public Notification(UUID id, UUID userId, NotificationType type, String title, String body, String link,
                         OffsetDateTime createdAt, OffsetDateTime readAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.body = body;
        this.link = link;
        this.createdAt = createdAt;
        this.readAt = readAt;
    }

    public static Notification create(UUID userId, NotificationType type, String title, String body, String link) {
        return new Notification(UUID.randomUUID(), userId, type, title, body, link, OffsetDateTime.now(), null);
    }

    public void markRead(OffsetDateTime now) {
        if (readAt == null) {
            readAt = now;
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getLink() {
        return link;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public boolean isUnread() {
        return readAt == null;
    }
}
