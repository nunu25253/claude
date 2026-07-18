package com.buzzanalysis.domain.script;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AIが生成した動画台本（Phase11）。Phase10の{@code ContentProposal}1件から、
 * 尺（30/60/90秒）ごとに複数生成できる。
 */
public final class VideoScript {

    private final UUID id;
    private final UUID proposalId;
    private final int durationSeconds;
    private final String bgmImage;
    private final String callToAction;
    private final List<ScriptCut> cuts;
    private final OffsetDateTime createdAt;

    private VideoScript(Builder b) {
        this.id = b.id;
        this.proposalId = b.proposalId;
        this.durationSeconds = b.durationSeconds;
        this.bgmImage = b.bgmImage;
        this.callToAction = b.callToAction;
        this.cuts = b.cuts == null ? List.of() : b.cuts;
        this.createdAt = b.createdAt;
    }

    public static Builder builder() {
        return new Builder();
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

    public static final class Builder {
        private UUID id;
        private UUID proposalId;
        private int durationSeconds;
        private String bgmImage;
        private String callToAction;
        private List<ScriptCut> cuts;
        private OffsetDateTime createdAt;

        public Builder id(UUID v) {
            this.id = v;
            return this;
        }

        public Builder proposalId(UUID v) {
            this.proposalId = v;
            return this;
        }

        public Builder durationSeconds(int v) {
            this.durationSeconds = v;
            return this;
        }

        public Builder bgmImage(String v) {
            this.bgmImage = v;
            return this;
        }

        public Builder callToAction(String v) {
            this.callToAction = v;
            return this;
        }

        public Builder cuts(List<ScriptCut> v) {
            this.cuts = v;
            return this;
        }

        public Builder createdAt(OffsetDateTime v) {
            this.createdAt = v;
            return this;
        }

        public VideoScript build() {
            return new VideoScript(this);
        }
    }
}
