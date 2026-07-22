package com.buzzanalysis.domain.auth;

import java.util.Optional;

/** PasswordResetToken集約のリポジトリインターフェース。 */
public interface PasswordResetTokenRepository {

    PasswordResetToken save(PasswordResetToken token);

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);
}
