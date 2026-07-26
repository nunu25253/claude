package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** パスワードリセットのメール送信依頼リクエスト。 */
public record PasswordResetRequestRequest(
        @NotBlank @Email String email
) {
}
