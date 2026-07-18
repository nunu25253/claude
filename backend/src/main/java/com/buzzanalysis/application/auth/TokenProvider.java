package com.buzzanalysis.application.auth;

import com.buzzanalysis.domain.user.User;

/**
 * JWTアクセストークン/リフレッシュトークンの発行・検証を行うポート。実装はinfrastructure層に置く。
 */
public interface TokenProvider {

    /** アクセストークンを発行する。 */
    IssuedToken generateAccessToken(User user);

    /** リフレッシュトークンを発行する。 */
    IssuedToken generateRefreshToken(User user);

    /**
     * リフレッシュトークンを検証し、有効であればユーザーIDを返す。
     *
     * @throws com.buzzanalysis.domain.common.exception.BusinessRuleViolationException トークンが無効/期限切れの場合
     */
    java.util.UUID validateRefreshTokenAndGetUserId(String refreshToken);

    /** 発行されたトークンとその有効期限（秒）。 */
    record IssuedToken(String token, long expiresInSeconds) {
    }
}
