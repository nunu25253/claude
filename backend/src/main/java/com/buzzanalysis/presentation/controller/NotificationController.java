package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.notification.NotificationApplicationService;
import com.buzzanalysis.application.notification.dto.NotificationDto;
import com.buzzanalysis.presentation.dto.response.UnreadNotificationCountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** アプリ内通知センターAPI。ログイン中のユーザー(JWT)に紐づく通知のみを扱う。 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "アプリ内通知センター")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;

    public NotificationController(NotificationApplicationService notificationApplicationService) {
        this.notificationApplicationService = notificationApplicationService;
    }

    @Operation(summary = "通知一覧取得(直近50件、新しい順)")
    @GetMapping
    public ResponseEntity<List<NotificationDto>> list(Authentication authentication) {
        return ResponseEntity.ok(notificationApplicationService.list(currentUserId(authentication)));
    }

    @Operation(summary = "未読通知数の取得")
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadNotificationCountResponse> unreadCount(Authentication authentication) {
        long count = notificationApplicationService.unreadCount(currentUserId(authentication));
        return ResponseEntity.ok(new UnreadNotificationCountResponse(count));
    }

    @Operation(summary = "通知を既読にする")
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(Authentication authentication, @PathVariable UUID id) {
        notificationApplicationService.markRead(currentUserId(authentication), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "すべての通知を既読にする")
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        notificationApplicationService.markAllRead(currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
