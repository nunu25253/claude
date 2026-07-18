package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/** アクセストークン再発行リクエスト。 */
public record RefreshRequest(@NotBlank String refreshToken) {
}
