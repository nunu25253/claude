package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.infrastructure.persistence.entity.OrganizationMembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Spring Data JPAによる {@link OrganizationMembershipEntity} の永続化アクセス。 */
public interface OrganizationMembershipJpaRepository extends JpaRepository<OrganizationMembershipEntity, UUID> {

    List<OrganizationMembershipEntity> findByOrganizationId(UUID organizationId);

    List<OrganizationMembershipEntity> findByUserId(UUID userId);

    Optional<OrganizationMembershipEntity> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);
}
