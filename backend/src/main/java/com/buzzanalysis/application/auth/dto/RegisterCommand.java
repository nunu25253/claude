package com.buzzanalysis.application.auth.dto;

/**
 * ユーザー登録ユースケースの入力コマンド。
 * {@code captchaToken}/{@code remoteIp}はBot対策(CAPTCHA)検証のための付随情報。
 */
public record RegisterCommand(String email, String rawPassword, String displayName, String captchaToken,
                               String remoteIp) {
}
