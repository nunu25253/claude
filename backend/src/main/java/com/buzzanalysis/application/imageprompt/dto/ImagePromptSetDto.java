package com.buzzanalysis.application.imageprompt.dto;

import com.buzzanalysis.domain.imageprompt.ImagePromptSet;
import com.buzzanalysis.domain.imageprompt.ImagePromptSourceType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@link ImagePromptSet}（ドメイン）のapplication層向けDTO。 */
public record ImagePromptSetDto(UUID id, ImagePromptSourceType sourceType, UUID sourceId,
                                 List<ImagePromptDto> prompts, OffsetDateTime createdAt) {
    public static ImagePromptSetDto from(ImagePromptSet s) {
        return new ImagePromptSetDto(s.getId(), s.getSourceType(), s.getSourceId(),
                s.getPrompts().stream().map(ImagePromptDto::from).toList(), s.getCreatedAt());
    }
}
