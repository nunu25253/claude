package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.settings.UserSettings;
import com.buzzanalysis.domain.settings.UserSettingsRepository;
import com.buzzanalysis.infrastructure.persistence.mapper.UserSettingsMapper;
import com.buzzanalysis.infrastructure.persistence.repository.UserSettingsJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/** {@link UserSettingsRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class UserSettingsRepositoryImpl implements UserSettingsRepository {

    private final UserSettingsJpaRepository jpaRepository;
    private final UserSettingsMapper mapper;

    public UserSettingsRepositoryImpl(UserSettingsJpaRepository jpaRepository, UserSettingsMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UserSettings save(UserSettings settings) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(settings)));
    }

    @Override
    public Optional<UserSettings> findByUserId(UUID userId) {
        return jpaRepository.findById(userId).map(mapper::toDomain);
    }
}
