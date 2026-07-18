package com.buzzanalysis.application.auth.dto;

import java.util.UUID;

/** 認証成功時の結果DTO（アクセストークン/リフレッシュトークンを含む）。 */
public record AuthResult(
        UUID userId,
        String email,
        String displayName,
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken,
        long refreshTokenExpiresInSeconds
) {
}
