package com.buzzanalysis.domain.rag;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * RAG検索対象として索引化された1件のテキストドキュメント（Phase16）。既存集約（分析結果/評価/
 * トレンドレポート等）から明示的に索引登録された内容を保持する（自動索引化はしない。設計doc参照）。
 */
public final class RagDocument {

    private final UUID id;
    private final RagSourceType sourceType;
    private final UUID sourceId;
    private final String contentText;
    private final float[] vector;
    private final String model;
    private final int dimensions;
    private final OffsetDateTime createdAt;

    private RagDocument(Builder b) {
        this.id = b.id;
        this.sourceType = b.sourceType;
        this.sourceId = b.sourceId;
        this.contentText = b.contentText;
        this.vector = b.vector;
        this.model = b.model;
        this.dimensions = b.dimensions;
        this.createdAt = b.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public RagSourceType getSourceType() {
        return sourceType;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getContentText() {
        return contentText;
    }

    public float[] getVector() {
        return vector;
    }

    public String getModel() {
        return model;
    }

    public int getDimensions() {
        return dimensions;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private UUID id;
        private RagSourceType sourceType;
        private UUID sourceId;
        private String contentText;
        private float[] vector;
        private String model;
        private int dimensions;
        private OffsetDateTime createdAt;

        public Builder id(UUID v) {
            this.id = v;
            return this;
        }

        public Builder sourceType(RagSourceType v) {
            this.sourceType = v;
            return this;
        }

        public Builder sourceId(UUID v) {
            this.sourceId = v;
            return this;
        }

        public Builder contentText(String v) {
            this.contentText = v;
            return this;
        }

        public Builder vector(float[] v) {
            this.vector = v;
            return this;
        }

        public Builder model(String v) {
            this.model = v;
            return this;
        }

        public Builder dimensions(int v) {
            this.dimensions = v;
            return this;
        }

        public Builder createdAt(OffsetDateTime v) {
            this.createdAt = v;
            return this;
        }

        public RagDocument build() {
            return new RagDocument(this);
        }
    }
}
