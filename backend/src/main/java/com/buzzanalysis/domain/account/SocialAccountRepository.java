package com.buzzanalysis.domain.account;

import com.buzzanalysis.domain.platform.Platform;

import java.util.Optional;
import java.util.UUID;

/** SocialAccount集約のリポジトリインターフェース。 */
public interface SocialAccountRepository {

    SocialAccount save(SocialAccount account);

    Optional<SocialAccount> findById(UUID id);

    Optional<SocialAccount> findByPlatformAndExternalAccountId(Platform platform, String externalAccountId);

    Optional<SocialAccount> findByPlatformAndUsername(Platform platform, String username);
}
