package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.imageprompt.ImagePromptSet;
import com.buzzanalysis.infrastructure.persistence.entity.ImagePromptSetEntity;
import org.springframework.stereotype.Component;

/** {@link ImagePromptSet}（ドメイン）と {@link ImagePromptSetEntity}（JPA）の相互変換を行う。 */
@Component
public class ImagePromptSetMapper {

    public ImagePromptSetEntity toEntity(ImagePromptSet s) {
        return new ImagePromptSetEntity(s.getId(), s.getSourceType(), s.getSourceId(), s.getPrompts(),
                s.getCreatedAt());
    }

    public ImagePromptSet toDomain(ImagePromptSetEntity entity) {
        return ImagePromptSet.builder()
                .id(entity.getId())
                .sourceType(entity.getSourceType())
                .sourceId(entity.getSourceId())
                .prompts(entity.getPrompts())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
