package com.buzzanalysis.application.script.dto;

import com.buzzanalysis.domain.script.VideoScript;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@link VideoScript}（ドメイン）のapplication層向けDTO。 */
public record VideoScriptDto(
        UUID id,
        UUID proposalId,
        int durationSeconds,
        String bgmImage,
        String callToAction,
        List<ScriptCutDto> cuts,
        OffsetDateTime createdAt
) {
    public static VideoScriptDto from(VideoScript s) {
        return new VideoScriptDto(
                s.getId(), s.getProposalId(), s.getDurationSeconds(), s.getBgmImage(), s.getCallToAction(),
                s.getCuts().stream().map(ScriptCutDto::from).toList(), s.getCreatedAt()
        );
    }
}
