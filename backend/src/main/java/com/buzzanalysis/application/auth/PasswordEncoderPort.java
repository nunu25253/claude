package com.buzzanalysis.application.auth;

/**
 * パスワードのハッシュ化/照合を行うポート（インターフェース）。
 * 実装（BCrypt等）はinfrastructure層に置く。
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
