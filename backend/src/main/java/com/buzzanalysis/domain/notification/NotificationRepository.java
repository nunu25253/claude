package com.buzzanalysis.domain.notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Notification集約のリポジトリインターフェース。 */
public interface NotificationRepository {

    Notification save(Notification notification);

    /** 指定ユーザーの通知を新しい順に、直近{@code limit}件だけ返す(通知一覧画面用)。 */
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, int limit);

    Optional<Notification> findById(UUID id);

    long countUnreadByUserId(UUID userId);

    /** 指定ユーザーの未読通知をすべて既読にする。 */
    void markAllReadByUserId(UUID userId);
}
