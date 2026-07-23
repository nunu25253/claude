package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.organization.Organization;
import com.buzzanalysis.infrastructure.persistence.entity.OrganizationEntity;
import org.springframework.stereotype.Component;

/** {@link Organization}（ドメイン）と {@link OrganizationEntity}（JPA）の相互変換を行う。 */
@Component
public class OrganizationMapper {

    public OrganizationEntity toEntity(Organization organization) {
        return new OrganizationEntity(organization.getId(), organization.getName(), organization.getCreatedAt());
    }

    public Organization toDomain(OrganizationEntity entity) {
        return new Organization(entity.getId(), entity.getName(), entity.getCreatedAt());
    }
}
