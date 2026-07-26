package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.organization.Organization;
import com.buzzanalysis.domain.organization.OrganizationRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.OrganizationMapper;
import com.buzzanalysis.infrastructure.persistence.repository.OrganizationJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** {@link OrganizationRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class OrganizationRepositoryImpl implements OrganizationRepository {

    private final OrganizationJpaRepository jpaRepository;
    private final OrganizationMapper mapper;

    public OrganizationRepositoryImpl(OrganizationJpaRepository jpaRepository, OrganizationMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Organization save(Organization organization) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(organization)));
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
