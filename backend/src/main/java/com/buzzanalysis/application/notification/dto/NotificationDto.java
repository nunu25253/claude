package com.buzzanalysis.application.notification.dto;

import com.buzzanalysis.domain.notification.Notification;
import com.buzzanalysis.domain.notification.NotificationType;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 通知一覧APIのレスポンスDTO。 */
public record NotificationDto(
        UUID id,
        NotificationType type,
        String title,
        String body,
        String link,
        OffsetDateTime createdAt,
        boolean unread
) {
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getBody(), notification.getLink(), notification.getCreatedAt(), notification.isUnread());
    }
}
