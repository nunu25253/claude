package com.buzzanalysis.infrastructure.persistence.mapper;

import com.buzzanalysis.domain.script.VideoScript;
import com.buzzanalysis.infrastructure.persistence.entity.VideoScriptEntity;
import org.springframework.stereotype.Component;

/** {@link VideoScript}（ドメイン）と {@link VideoScriptEntity}（JPA）の相互変換を行う。 */
@Component
public class VideoScriptMapper {

    public VideoScriptEntity toEntity(VideoScript s) {
        return new VideoScriptEntity(s.getId(), s.getProposalId(), s.getDurationSeconds(), s.getBgmImage(),
                s.getCallToAction(), s.getCuts(), s.getCreatedAt());
    }

    public VideoScript toDomain(VideoScriptEntity entity) {
        return VideoScript.builder()
                .id(entity.getId())
                .proposalId(entity.getProposalId())
                .durationSeconds(entity.getDurationSeconds())
                .bgmImage(entity.getBgmImage())
                .callToAction(entity.getCallToAction())
                .cuts(entity.getCuts())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
