package com.buzzanalysis.domain.imageprompt;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * AIが生成した画像生成プロンプト一式（Phase13）。Phase11の{@code VideoScript}またはPhase12の
 * {@code Carousel}1件から、各要素の{@code visualDirection}を元に生成される。実際の画像生成は
 * 行わず、プロンプト文字列のみを成果物とする（コスト・著作権上の判断。設計doc参照）。
 */
public final class ImagePromptSet {

    private final UUID id;
    private final ImagePromptSourceType sourceType;
    private final UUID sourceId;
    private final List<ImagePrompt> prompts;
    private final OffsetDateTime createdAt;

    private ImagePromptSet(Builder b) {
        this.id = b.id;
        this.sourceType = b.sourceType;
        this.sourceId = b.sourceId;
        this.prompts = b.prompts == null ? List.of() : b.prompts;
        this.createdAt = b.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public ImagePromptSourceType getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public List<ImagePrompt> getPrompts() {
        return prompts;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private UUID id;
        private ImagePromptSourceType sourceType;
        private UUID sourceId;
        private List<ImagePrompt> prompts;
        private OffsetDateTime createdAt;

        public Builder id(UUID v) {
            this.id = v;
            return this;
        }

        public Builder sourceType(ImagePromptSourceType v) {
            this.sourceType = v;
            return this;
        }

        public Builder sourceId(UUID v) {
            this.sourceId = v;
            return this;
        }

        public Builder prompts(List<ImagePrompt> v) {
            this.prompts = v;
            return this;
        }

        public Builder createdAt(OffsetDateTime v) {
            this.createdAt = v;
            return this;
        }

        public ImagePromptSet build() {
            return new ImagePromptSet(this);
        }
    }
}
