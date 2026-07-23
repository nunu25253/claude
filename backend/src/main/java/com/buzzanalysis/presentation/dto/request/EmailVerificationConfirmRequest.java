package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/** メールアドレス確認の確定リクエスト。 */
public record EmailVerificationConfirmRequest(
        @NotBlank String token
) {
}
