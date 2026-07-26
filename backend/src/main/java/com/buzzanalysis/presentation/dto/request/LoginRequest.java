package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** ログインリクエスト。 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
