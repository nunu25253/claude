package com.buzzanalysis.domain.auth;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 登録時のメールアドレス確認用ワンタイムトークン。設計は{@link PasswordResetToken}と同じ
 * (SHA-256でハッシュ化した値を保存し、検索可能なハッシュとして使う)。
 */
public class EmailVerificationToken {

    private final UUID id;
    private final UUID userId;
    private final String tokenHash;
    private final OffsetDateTime expiresAt;
    private OffsetDateTime usedAt;
    private final OffsetDateTime createdAt;

    public EmailVerificationToken(UUID id, UUID userId, String tokenHash, OffsetDateTime expiresAt,
                                   OffsetDateTime usedAt, OffsetDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
        this.createdAt = createdAt;
    }

    public static EmailVerificationToken createNew(UUID userId, String tokenHash, Duration validFor) {
        OffsetDateTime now = OffsetDateTime.now();
        return new EmailVerificationToken(UUID.randomUUID(), userId, tokenHash, now.plus(validFor), null, now);
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
