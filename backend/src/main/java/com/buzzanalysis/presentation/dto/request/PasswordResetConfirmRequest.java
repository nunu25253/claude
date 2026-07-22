package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** パスワードリセットの確定（新パスワード設定）リクエスト。 */
public record PasswordResetConfirmRequest(
        @NotBlank String token,
        @NotBlank @Size(min = 8, max = 100, message = "password must be between 8 and 100 characters") String newPassword
) {
}
