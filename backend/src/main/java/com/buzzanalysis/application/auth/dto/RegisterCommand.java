package com.buzzanalysis.application.auth.dto;

/** ユーザー登録ユースケースの入力コマンド。 */
public record RegisterCommand(String email, String rawPassword, String displayName) {
}
