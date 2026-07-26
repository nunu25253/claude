package com.buzzanalysis.domain.notification;

/** アプリ内通知の種別。メール通知(MailSenderPort)と対になるイベントに対応する。 */
public enum NotificationType {
    THRESHOLD_ALERT,
    WEEKLY_DIGEST
}
