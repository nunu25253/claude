package com.buzzanalysis.application.settings.dto;

/**
 * プロフィール更新リクエスト。フロントエンドは{@code email}も一緒に送るが、メールアドレスは
 * ログインIDを兼ねるため{@code User}集約上は不変（{@link com.buzzanalysis.domain.user.User}参照）。
 * 本APIでは{@code displayName}のみを反映し、{@code email}は無視する。
 */
public record UpdateProfileRequest(String displayName, String email) {
}
