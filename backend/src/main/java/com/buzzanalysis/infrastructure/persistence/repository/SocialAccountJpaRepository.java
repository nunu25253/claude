package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.PlatformEnum;
import com.buzzanalysis.infrastructure.persistence.entity.SocialAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link SocialAccountEntity} の永続化アクセス。 */
public interface SocialAccountJpaRepository extends JpaRepository<SocialAccountEntity, UUID> {

    Optional<SocialAccountEntity> findByPlatformAndExternalAccountId(PlatformEnum platform, String externalAccountId);

    Optional<SocialAccountEntity> findByPlatformAndUsername(PlatformEnum platform, String username);

    List<SocialAccountEntity> findByTrackingEnabledTrue();
}
