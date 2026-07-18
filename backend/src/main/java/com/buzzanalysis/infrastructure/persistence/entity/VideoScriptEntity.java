package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.domain.script.ScriptCut;
import com.buzzanalysis.infrastructure.persistence.converter.ScriptCutListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@code video_scripts} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "video_scripts")
public class VideoScriptEntity {

    @Id
    private UUID id;

    @Column(name = "proposal_id", nullable = false)
    private UUID proposalId;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds;

    @Column(name = "bgm_image", columnDefinition = "TEXT")
    private String bgmImage;

    @Column(name = "call_to_action", columnDefinition = "TEXT")
    private String callToAction;

    @Convert(converter = ScriptCutListJsonConverter.class)
    @Column(name = "cuts", columnDefinition = "TEXT", nullable = false)
    private List<ScriptCut> cuts;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected VideoScriptEntity() {
    }

    public VideoScriptEntity(UUID id, UUID proposalId, int durationSeconds, String bgmImage, String callToAction,
                              List<ScriptCut> cuts, OffsetDateTime createdAt) {
        this.id = id;
        this.proposalId = proposalId;
        this.durationSeconds = durationSeconds;
        this.bgmImage = bgmImage;
        this.callToAction = callToAction;
        this.cuts = cuts;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProposalId() {
        return proposalId;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public String getBgmImage() {
        return bgmImage;
    }

    public String getCallToAction() {
        return callToAction;
    }

    public List<ScriptCut> getCuts() {
        return cuts;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
