package com.buzzanalysis.infrastructure.persistence.entity;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.trend.TrendItem;
import com.buzzanalysis.infrastructure.persistence.converter.TrendItemListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** {@code trend_reports} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "trend_reports")
public class TrendReportEntity {

    @Id
    private UUID id;

    @Column(name = "platform", length = 30)
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(name = "recent_window_days", nullable = false)
    private int recentWindowDays;

    @Column(name = "baseline_window_days", nullable = false)
    private int baselineWindowDays;

    @Convert(converter = TrendItemListJsonConverter.class)
    @Column(name = "items", columnDefinition = "TEXT", nullable = false)
    private List<TrendItem> items;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected TrendReportEntity() {
    }

    public TrendReportEntity(UUID id, Platform platform, int recentWindowDays, int baselineWindowDays,
                              List<TrendItem> items, String aiSummary, OffsetDateTime createdAt) {
        this.id = id;
        this.platform = platform;
        this.recentWindowDays = recentWindowDays;
        this.baselineWindowDays = baselineWindowDays;
        this.items = items;
        this.aiSummary = aiSummary;
        this.createdAt = createdAt;
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
}
