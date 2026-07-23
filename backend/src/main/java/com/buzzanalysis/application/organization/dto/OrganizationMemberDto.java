package com.buzzanalysis.application.organization.dto;

import com.buzzanalysis.domain.organization.OrganizationRole;

import java.time.OffsetDateTime;
import java.util.UUID;

/** 組織メンバー1名分のapplication層向けDTO。 */
public record OrganizationMemberDto(
        UUID userId,
        String email,
        String displayName,
        OrganizationRole role,
        OffsetDateTime joinedAt
) {
}
