package com.buzzanalysis.domain.trend;

import com.buzzanalysis.domain.platform.Platform;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * トレンド分析レポート（Phase15）。直近ウィンドウとベースラインウィンドウの2期間比較により、
 * 急上昇しているハッシュタグ/ジャンル/コンテンツ形式を検出する（すべて決定的な統計計算）。
 */
public final class TrendReport {

    private final UUID id;
    private final Platform platform;
    private final int recentWindowDays;
    private final int baselineWindowDays;
    private final List<TrendItem> items;
    private final String aiSummary;
    private final OffsetDateTime createdAt;

    private TrendReport(Builder b) {
        this.id = b.id;
        this.platform = b.platform;
        this.recentWindowDays = b.recentWindowDays;
        this.baselineWindowDays = b.baselineWindowDays;
        this.items = b.items == null ? List.of() : b.items;
        this.aiSummary = b.aiSummary;
        this.createdAt = b.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public Platform getPlatform() {
        return platform;
    }

    public int getRecentWindowDays() {
        return recentWindowDays;
    }

    public int getBaselineWindowDays() {
        return baselineWindowDays;
    }

    public List<TrendItem> getItems() {
        return items;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public static final class Builder {
        private UUID id;
        private Platform platform;
        private int recentWindowDays;
        private int baselineWindowDays;
        private List<TrendItem> items;
        private String aiSummary;
        private OffsetDateTime createdAt;

        public Builder id(UUID v) {
            this.id = v;
            return this;
        }

        public Builder platform(Platform v) {
            this.platform = v;
            return this;
        }

        public Builder recentWindowDays(int v) {
            this.recentWindowDays = v;
            return this;
        }

        public Builder baselineWindowDays(int v) {
            this.baselineWindowDays = v;
            return this;
        }

        public Builder items(List<TrendItem> v) {
            this.items = v;
            return this;
        }

        public Builder aiSummary(String v) {
            this.aiSummary = v;
            return this;
        }

        public Builder createdAt(OffsetDateTime v) {
            this.createdAt = v;
            return this;
        }

        public TrendReport build() {
            return new TrendReport(this);
        }
    }
}
