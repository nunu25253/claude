package com.buzzanalysis.application.auth.dto;

/** ログインユースケースの入力コマンド。 */
public record LoginCommand(String email, String rawPassword) {
}
