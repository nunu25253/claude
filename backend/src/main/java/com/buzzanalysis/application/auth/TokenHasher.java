package com.buzzanalysis.application.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * パスワードリセット・メールアドレス確認で共通利用するワンタイムトークンのユーティリティ。
 * トークン自体は{@link SecureRandom}由来の高エントロピーな乱数のため、パスワードのような
 * 遅いソルト付きハッシュ(BCrypt等)は不要で、検索可能なSHA-256のハッシュ値をDBに保存する。
 */
final class TokenHasher {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private TokenHasher() {
    }

    static String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
