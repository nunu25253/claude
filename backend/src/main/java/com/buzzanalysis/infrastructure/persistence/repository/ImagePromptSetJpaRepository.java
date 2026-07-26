package com.buzzanalysis.infrastructure.persistence.repository;

import com.buzzanalysis.domain.imageprompt.ImagePromptSourceType;
import com.buzzanalysis.infrastructure.persistence.entity.ImagePromptSetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/** Spring Data JPAによる {@link ImagePromptSetEntity} の永続化アクセス。 */
public interface ImagePromptSetJpaRepository extends JpaRepository<ImagePromptSetEntity, UUID> {

    List<ImagePromptSetEntity> findBySourceTypeAndSourceId(ImagePromptSourceType sourceType, UUID sourceId);
}
