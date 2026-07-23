package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.organization.OrganizationMembership;
import com.buzzanalysis.infrastructure.persistence.entity.OrganizationMembershipEntity;
import org.springframework.stereotype.Component;

/** {@link OrganizationMembership}（ドメイン）と {@link OrganizationMembershipEntity}（JPA）の相互変換を行う。 */
@Component
public class OrganizationMembershipMapper {

    public OrganizationMembershipEntity toEntity(OrganizationMembership membership) {
        return new OrganizationMembershipEntity(membership.getId(), membership.getOrganizationId(),
                membership.getUserId(), membership.getRole(), membership.getJoinedAt());
    }

    public OrganizationMembership toDomain(OrganizationMembershipEntity entity) {
        return new OrganizationMembership(entity.getId(), entity.getOrganizationId(), entity.getUserId(),
                entity.getRole(), entity.getJoinedAt());
    }
}
