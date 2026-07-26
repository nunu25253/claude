package com.buzzanalysis.domain.organization;

import java.time.OffsetDateTime;
import java.util.UUID;

/** ユーザーと組織の所属関係(1組織に対してユーザーごとに1件、UNIQUE制約あり)。 */
public class OrganizationMembership {

    private final UUID id;
    private final UUID organizationId;
    private final UUID userId;
    private final OrganizationRole role;
    private final OffsetDateTime joinedAt;

    public OrganizationMembership(UUID id, UUID organizationId, UUID userId, OrganizationRole role,
                                   OffsetDateTime joinedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public static OrganizationMembership createNew(UUID organizationId, UUID userId, OrganizationRole role) {
        return new OrganizationMembership(UUID.randomUUID(), organizationId, userId, role, OffsetDateTime.now());
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

    public OrganizationRole getRole() {
        return role;
    }

    public OffsetDateTime getJoinedAt() {
        return joinedAt;
    }
}
