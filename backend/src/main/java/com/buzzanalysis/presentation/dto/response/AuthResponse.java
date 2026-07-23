package com.buzzanalysis.presentation.dto.response;

import com.buzzanalysis.application.auth.dto.AuthResult;

import java.util.UUID;

/**
 * 認証成功時にクライアントへ返すレスポンス。生のアクセス/リフレッシュトークンは含めない
 * （トークンはHttpOnly Cookieとして{@code Set-Cookie}ヘッダーで別途送る。JSONボディに含めると
 * ブラウザのJSやログ等からトークンに触れる経路が増えてしまうため）。
 */
public record AuthResponse(
        UUID userId,
        String email,
        String displayName,
        boolean emailVerified,
        long accessTokenExpiresInSeconds
) {
    public static AuthResponse from(AuthResult result) {
        return new AuthResponse(result.userId(), result.email(), result.displayName(), result.emailVerified(),
                result.accessTokenExpiresInSeconds());
    }
}
