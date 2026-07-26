package com.buzzanalysis.domain.organization;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** OrganizationMembershipのリポジトリインターフェース。 */
public interface OrganizationMembershipRepository {

    OrganizationMembership save(OrganizationMembership membership);

    List<OrganizationMembership> findByOrganizationId(UUID organizationId);

    /** 指定ユーザーが所属する全組織のメンバーシップを返す(=ユーザーが所属する組織一覧)。 */
    List<OrganizationMembership> findByUserId(UUID userId);

    Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

    void delete(OrganizationMembership membership);
}
