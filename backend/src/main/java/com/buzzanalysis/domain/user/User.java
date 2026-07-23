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
    private boolean emailVerified;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public User(UUID id, String email, String passwordHash, String displayName, Role role, boolean emailVerified,
                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.displayName = displayName;
        this.role = role == null ? Role.USER : role;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 新規登録時点ではメールアドレス未確認(emailVerified=false)として作成する。 */
    public static User createNew(String email, String hashedPassword, String displayName) {
        OffsetDateTime now = OffsetDateTime.now();
        return new User(UUID.randomUUID(), email, hashedPassword, displayName, Role.USER, false, now, now);
    }

    public void changePassword(String newHashedPassword) {
        this.passwordHash = Objects.requireNonNull(newHashedPassword);
        this.updatedAt = OffsetDateTime.now();
    }

    /** 表示名を更新する。メールアドレスはログインIDを兼ねるため不変とし、本メソッドの対象外とする。 */
    public void updateDisplayName(String newDisplayName) {
        this.displayName = newDisplayName;
        this.updatedAt = OffsetDateTime.now();
    }

    public void verifyEmail() {
        this.emailVerified = true;
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

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
