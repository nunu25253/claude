package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.auth.PasswordResetToken;
import com.buzzanalysis.domain.auth.PasswordResetTokenRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.PasswordResetTokenMapper;
import com.buzzanalysis.infrastructure.persistence.repository.PasswordResetTokenJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** {@link PasswordResetTokenRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;
    private final PasswordResetTokenMapper mapper;

    public PasswordResetTokenRepositoryImpl(PasswordResetTokenJpaRepository jpaRepository, PasswordResetTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(token)));
    }

    @Override
    public Optional<PasswordResetToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }
}
