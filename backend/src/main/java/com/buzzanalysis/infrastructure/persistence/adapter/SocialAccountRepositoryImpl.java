package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.account.SocialAccount;
import com.buzzanalysis.domain.account.SocialAccountRepository;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.infrastructure.persistence.mapper.PlatformMapper;
import com.buzzanalysis.infrastructure.persistence.mapper.SocialAccountMapper;
import com.buzzanalysis.infrastructure.persistence.repository.SocialAccountJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** {@link SocialAccountRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class SocialAccountRepositoryImpl implements SocialAccountRepository {

    private final SocialAccountJpaRepository jpaRepository;
    private final SocialAccountMapper mapper;
    private final PlatformMapper platformMapper;

    public SocialAccountRepositoryImpl(SocialAccountJpaRepository jpaRepository, SocialAccountMapper mapper,
                                        PlatformMapper platformMapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
        this.platformMapper = platformMapper;
    }

    @Override
    public SocialAccount save(SocialAccount account) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(account)));
    }

    @Override
    public Optional<SocialAccount> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<SocialAccount> findByPlatformAndExternalAccountId(Platform platform, String externalAccountId) {
        return jpaRepository.findByPlatformAndExternalAccountId(platformMapper.toEntity(platform), externalAccountId)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<SocialAccount> findByPlatformAndUsername(Platform platform, String username) {
        return jpaRepository.findByPlatformAndUsername(platformMapper.toEntity(platform), username)
                .map(mapper::toDomain);
    }
}
