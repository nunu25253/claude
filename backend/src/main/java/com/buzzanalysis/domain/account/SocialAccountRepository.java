package com.buzzanalysis.domain.account;

import com.buzzanalysis.domain.platform.Platform;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** SocialAccount集約のリポジトリインターフェース。 */
public interface SocialAccountRepository {

    SocialAccount save(SocialAccount account);

    Optional<SocialAccount> findById(UUID id);

    Optional<SocialAccount> findByPlatformAndExternalAccountId(Platform platform, String externalAccountId);

    Optional<SocialAccount> findByPlatformAndUsername(Platform platform, String username);

    /** 定期データ取得バッチ（自動同期）の対象となっている全アカウントを返す。 */
    List<SocialAccount> findAllTrackingEnabled();
}
