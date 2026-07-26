package com.buzzanalysis.domain.auth;

import java.util.Optional;

/** EmailVerificationToken集約のリポジトリインターフェース。 */
public interface EmailVerificationTokenRepository {

    EmailVerificationToken save(EmailVerificationToken token);

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);
}
