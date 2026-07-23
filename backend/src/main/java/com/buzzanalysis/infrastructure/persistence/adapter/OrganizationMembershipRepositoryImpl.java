package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.organization.OrganizationMembership;
import com.buzzanalysis.domain.organization.OrganizationMembershipRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.OrganizationMembershipMapper;
import com.buzzanalysis.infrastructure.persistence.repository.OrganizationMembershipJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** {@link OrganizationMembershipRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class OrganizationMembershipRepositoryImpl implements OrganizationMembershipRepository {

    private final OrganizationMembershipJpaRepository jpaRepository;
    private final OrganizationMembershipMapper mapper;

    public OrganizationMembershipRepositoryImpl(OrganizationMembershipJpaRepository jpaRepository,
                                                 OrganizationMembershipMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public OrganizationMembership save(OrganizationMembership membership) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(membership)));
    }

    @Override
    public List<OrganizationMembership> findByOrganizationId(UUID organizationId) {
        return jpaRepository.findByOrganizationId(organizationId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<OrganizationMembership> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<OrganizationMembership> findByOrganizationIdAndUserId(UUID organizationId, UUID userId) {
        return jpaRepository.findByOrganizationIdAndUserId(organizationId, userId).map(mapper::toDomain);
    }

    @Override
    public void delete(OrganizationMembership membership) {
        jpaRepository.deleteById(membership.getId());
    }
}
