package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** メールアドレス確認メールの再送依頼リクエスト。 */
public record EmailVerificationResendRequest(
        @NotBlank @Email String email
) {
}
