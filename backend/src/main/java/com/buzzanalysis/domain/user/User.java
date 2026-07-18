package com.buzzanalysis.domain.user;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * JWT認証の主体となるユーザー集約。フレームワーク非依存のPOJO。
 * パスワードは呼び出し側で既にハッシュ化された文字列として保持する（ハッシュ化自体はinfrastructure層の責務）。
 */
public class User {

    private final UUID id;
    private final String email;
    private String passwordHash;
    private String displayName;
    private Role role;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public User(UUID id, String email, String passwordHash, String displayName, Role role,
                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.displayName = displayName;
        this.role = role == null ? Role.USER : role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User createNew(String email, String hashedPassword, String displayName) {
        OffsetDateTime now = OffsetDateTime.now();
        return new User(UUID.randomUUID(), email, hashedPassword, displayName, Role.USER, now, now);
    }

    public void changePassword(String newHashedPassword) {
        this.passwordHash = Objects.requireNonNull(newHashedPassword);
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Role getRole() {
        return role;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
