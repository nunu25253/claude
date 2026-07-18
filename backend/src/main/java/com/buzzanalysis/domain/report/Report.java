package com.buzzanalysis.domain.report;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * AIレポート（PDF/Markdown/HTML）を表す集約。生成物のバイト列はS3等のオブジェクトストレージに保存し、
 * このエンティティ自体はメタデータ（フォーマット・保存先キー）を保持する。
 * 構築項目が多いため {@link Builder} を用いる（Builderパターン）。
 */
public final class Report {

    private final UUID id;
    private final UUID postId;
    private final UUID userId;
    private final ReportFormat format;
    private final String title;
    private final String storageKey;
    private final long contentSizeBytes;
    private final OffsetDateTime generatedAt;

    private Report(Builder b) {
        this.id = b.id != null ? b.id : UUID.randomUUID();
        this.postId = Objects.requireNonNull(b.postId, "postId must not be null");
        this.userId = b.userId;
        this.format = Objects.requireNonNull(b.format, "format must not be null");
        this.title = b.title;
        this.storageKey = Objects.requireNonNull(b.storageKey, "storageKey must not be null");
        this.contentSizeBytes = b.contentSizeBytes;
        this.generatedAt = b.generatedAt != null ? b.generatedAt : OffsetDateTime.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Reportを段階的に組み立てるBuilder。 */
    public static final class Builder {
        private UUID id;
        private UUID postId;
        private UUID userId;
        private ReportFormat format;
        private String title;
        private String storageKey;
        private long contentSizeBytes;
        private OffsetDateTime generatedAt;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder postId(UUID postId) {
            this.postId = postId;
            return this;
        }

        /** レポートを生成したユーザー。既存(移行前)データとの互換のためnull許容。 */
        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder format(ReportFormat format) {
            this.format = format;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder storageKey(String storageKey) {
            this.storageKey = storageKey;
            return this;
        }

        public Builder contentSizeBytes(long size) {
            this.contentSizeBytes = size;
            return this;
        }

        public Builder generatedAt(OffsetDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public Report build() {
            return new Report(this);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getPostId() {
        return postId;
    }

    public UUID getUserId() {
        return userId;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public String getTitle() {
        return title;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public long getContentSizeBytes() {
        return contentSizeBytes;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }
}
