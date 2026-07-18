package com.buzzanalysis.application.imageprompt.dto;

import com.buzzanalysis.domain.imageprompt.ImagePrompt;

/** {@link ImagePrompt}（ドメイン）のapplication層向けDTO。 */
public record ImagePromptDto(int index, String originalDirection, String generatedPrompt) {
    public static ImagePromptDto from(ImagePrompt p) {
        return new ImagePromptDto(p.index(), p.originalDirection(), p.generatedPrompt());
    }
}
