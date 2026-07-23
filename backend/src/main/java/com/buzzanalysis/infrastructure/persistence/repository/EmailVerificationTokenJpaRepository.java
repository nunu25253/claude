package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.EmailVerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link EmailVerificationTokenEntity} の永続化アクセス。 */
public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenEntity, UUID> {

    Optional<EmailVerificationTokenEntity> findByTokenHash(String tokenHash);
}
