package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.UserSettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Spring Data JPAによる {@link UserSettingsEntity} の永続化アクセス。 */
public interface UserSettingsJpaRepository extends JpaRepository<UserSettingsEntity, UUID> {
}
