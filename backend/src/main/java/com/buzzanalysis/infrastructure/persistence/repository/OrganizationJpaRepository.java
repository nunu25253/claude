package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Spring Data JPAによる {@link OrganizationEntity} の永続化アクセス。 */
public interface OrganizationJpaRepository extends JpaRepository<OrganizationEntity, UUID> {
}
