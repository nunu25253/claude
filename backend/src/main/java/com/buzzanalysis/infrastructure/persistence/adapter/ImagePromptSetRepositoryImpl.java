package com.buzzanalysis.infrastructure.persistence.adapter;

import com.buzzanalysis.domain.imageprompt.ImagePromptSet;
import com.buzzanalysis.domain.imageprompt.ImagePromptSetRepository;
import com.buzzanalysis.domain.imageprompt.ImagePromptSourceType;
import com.buzzanalysis.infrastructure.persistence.mapper.ImagePromptSetMapper;
import com.buzzanalysis.infrastructure.persistence.repository.ImagePromptSetJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/** {@link ImagePromptSetRepository} のJPA実装（Repositoryパターン）。 */
@Repository
public class ImagePromptSetRepositoryImpl implements ImagePromptSetRepository {

    private final ImagePromptSetJpaRepository jpaRepository;
    private final ImagePromptSetMapper mapper;

    public ImagePromptSetRepositoryImpl(ImagePromptSetJpaRepository jpaRepository, ImagePromptSetMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public ImagePromptSet save(ImagePromptSet promptSet) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(promptSet)));
    }

    @Override
    public List<ImagePromptSet> findBySource(ImagePromptSourceType sourceType, UUID sourceId) {
        return jpaRepository.findBySourceTypeAndSourceId(sourceType, sourceId).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
