package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link UserEntity} の永続化アクセス。 */
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

    List<UserEntity> findByIdIn(List<UUID> ids);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
