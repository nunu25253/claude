package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ユーザー登録リクエスト。{@code captchaToken}はCloudflare Turnstileウィジェットが発行するトークン。
 * {@code app.captcha.enabled=false}(既定)の環境では未指定でも登録できる。
 */
public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters") String password,
        @NotBlank @Size(max = 100) String displayName,
        String captchaToken
) {
}
