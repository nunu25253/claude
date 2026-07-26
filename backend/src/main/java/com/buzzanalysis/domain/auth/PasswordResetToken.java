package com.buzzanalysis.domain.auth;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * パスワードリセット用ワンタイムトークンを表す集約。
 * {@code tokenHash} はメールで送る生トークンをSHA-256でハッシュ化した値(検索可能なハッシュ)であり、
 * パスワードのようなBCryptは使わない(BCryptは検証のたびに異なるソルトを生成するためハッシュ値での
 * 検索ができない。トークン自体が既に高エントロピーな乱数のため、遅いハッシュ関数による保護は不要)。
 */
public class PasswordResetToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;
    private final OffsetDateTime createdAt;

    public PasswordResetToken(UUID id, UUID userId, String tokenHash, OffsetDateTime expiresAt,
                               OffsetDateTime usedAt, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
    }

    public static PasswordResetToken createNew(UUID userId, String tokenHash, Duration validFor) {
        OffsetDateTime now = OffsetDateTime.now();
        return new PasswordResetToken(UUID.randomUUID(), userId, tokenHash, now.plus(validFor), null, now);
    }

    public boolean isValid() {
        return usedAt == null && expiresAt.isAfter(OffsetDateTime.now());
    }

    public void markUsed() {
        this.usedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public OffsetDateTime getUsedAt() {
        return usedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
