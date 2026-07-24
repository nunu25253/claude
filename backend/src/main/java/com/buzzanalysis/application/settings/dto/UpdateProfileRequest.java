package com.buzzanalysis.application.settings.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * プロフィール更新リクエスト。フロントエンドは{@code email}も一緒に送るが、メールアドレスは
 * ログインIDを兼ねるため{@code User}集約上は不変（{@link com.buzzanalysis.domain.user.User}参照）。
 * 本APIでは{@code displayName}のみを反映し、{@code email}は無視する。
 */
public record UpdateProfileRequest(@NotBlank @Size(max = 100) String displayName, String email) {
}
