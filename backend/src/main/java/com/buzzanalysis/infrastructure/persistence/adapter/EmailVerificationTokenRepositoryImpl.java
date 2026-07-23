package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.auth.EmailVerificationToken;
import com.buzzanalysis.domain.auth.EmailVerificationTokenRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.EmailVerificationTokenMapper;
import com.buzzanalysis.infrastructure.persistence.repository.EmailVerificationTokenJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** {@link EmailVerificationTokenRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class EmailVerificationTokenRepositoryImpl implements EmailVerificationTokenRepository {

    private final EmailVerificationTokenJpaRepository jpaRepository;
    private final EmailVerificationTokenMapper mapper;

    public EmailVerificationTokenRepositoryImpl(EmailVerificationTokenJpaRepository jpaRepository,
                                                 EmailVerificationTokenMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public EmailVerificationToken save(EmailVerificationToken token) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(token)));
    }

    @Override
    public Optional<EmailVerificationToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(mapper::toDomain);
    }
}
