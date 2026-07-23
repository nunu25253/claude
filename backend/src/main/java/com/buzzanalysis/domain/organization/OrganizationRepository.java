package com.buzzanalysis.domain.organization;

import java.util.Optional;
import java.util.UUID;

/** Organization集約のリポジトリインターフェース。 */
public interface OrganizationRepository {

    Organization save(Organization organization);

    Optional<Organization> findById(UUID id);
}
