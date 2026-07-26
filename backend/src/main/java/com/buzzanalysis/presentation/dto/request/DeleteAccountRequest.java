package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/** アカウント削除(退会)の確定リクエスト。誤操作防止のため現在のパスワードの再入力を必須とする。 */
public record DeleteAccountRequest(
        @NotBlank String currentPassword
) {
}
