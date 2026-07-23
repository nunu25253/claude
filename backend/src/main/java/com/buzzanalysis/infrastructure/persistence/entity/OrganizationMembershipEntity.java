package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code organization_memberships} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "organization_memberships")
public class OrganizationMembershipEntity {

    @Id
    private UUID id;

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private com.buzzanalysis.domain.organization.OrganizationRole role;

    @Column(name = "joined_at", nullable = false)
    private OffsetDateTime joinedAt;

    protected OrganizationMembershipEntity() {
    }

    public OrganizationMembershipEntity(UUID id, UUID organizationId, UUID userId,
                                         com.buzzanalysis.domain.organization.OrganizationRole role,
                                         OffsetDateTime joinedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public UUID getUserId() {
        return userId;
    }

    public com.buzzanalysis.domain.organization.OrganizationRole getRole() {
        return role;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }
}
