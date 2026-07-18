package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.settings.SettingsApplicationService;
import com.buzzanalysis.application.settings.dto.ApiKeySettingsDto;
import com.buzzanalysis.application.settings.dto.NotificationSettingsDto;
import com.buzzanalysis.application.settings.dto.ProfileSettingsDto;
import com.buzzanalysis.application.settings.dto.UpdateProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * ユーザー設定API。プロフィール（表示名）・通知設定・APIキーを、ログイン中のユーザー（JWT）に
 * 紐づけて取得・更新する。
 */
@RestController
@RequestMapping("/api/v1/settings")
@Tag(name = "Settings", description = "ユーザー設定")
public class SettingsController {

    private final SettingsApplicationService settingsApplicationService;

    public SettingsController(SettingsApplicationService settingsApplicationService) {
        this.settingsApplicationService = settingsApplicationService;
    }

    @Operation(summary = "プロフィール取得")
    @GetMapping("/profile")
    public ResponseEntity<ProfileSettingsDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(settingsApplicationService.getProfile(currentUserId(authentication)));
    }

    @Operation(summary = "プロフィール更新", description = "表示名のみ更新可能。メールアドレスはログインIDを兼ねるため変更不可。")
    @PutMapping("/profile")
    public ResponseEntity<ProfileSettingsDto> updateProfile(Authentication authentication,
                                                              @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(settingsApplicationService.updateProfile(currentUserId(authentication), request));
    }

    @Operation(summary = "通知設定取得")
    @GetMapping("/notifications")
    public ResponseEntity<NotificationSettingsDto> getNotifications(Authentication authentication) {
        return ResponseEntity.ok(settingsApplicationService.getNotifications(currentUserId(authentication)));
    }

    @Operation(summary = "通知設定更新")
    @PutMapping("/notifications")
    public ResponseEntity<NotificationSettingsDto> updateNotifications(Authentication authentication,
                                                                        @RequestBody NotificationSettingsDto request) {
        return ResponseEntity.ok(
                settingsApplicationService.updateNotifications(currentUserId(authentication), request));
    }

    @Operation(summary = "APIキー取得", description = "未発行の場合はapiKey=nullを返す。")
    @GetMapping("/api-key")
    public ResponseEntity<ApiKeySettingsDto> getApiKey(Authentication authentication) {
        return ResponseEntity.ok(settingsApplicationService.getApiKey(currentUserId(authentication)));
    }

    @Operation(summary = "APIキー再発行")
    @PostMapping("/api-key/regenerate")
    public ResponseEntity<ApiKeySettingsDto> regenerateApiKey(Authentication authentication) {
        return ResponseEntity.ok(settingsApplicationService.regenerateApiKey(currentUserId(authentication)));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
