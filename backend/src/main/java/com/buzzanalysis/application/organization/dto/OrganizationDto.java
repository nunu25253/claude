package com.buzzanalysis.application.organization.dto;

import com.buzzanalysis.domain.organization.Organization;
import com.buzzanalysis.domain.organization.OrganizationRole;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 組織のapplication層向けDTO。myRoleはリクエストしたユーザー自身のその組織内での権限。 */
public record OrganizationDto(UUID id, String name, OffsetDateTime createdAt, OrganizationRole myRole) {

    public static OrganizationDto from(Organization organization, OrganizationRole myRole) {
        return new OrganizationDto(organization.getId(), organization.getName(), organization.getCreatedAt(), myRole);
    }
}
