package com.buzzanalysis.application.trend.dto;

import com.buzzanalysis.domain.platform.Platform;

/**
 * トレンド分析APIのリクエストDTO（Phase15）。{@code platform}省略時は全プラットフォーム対象。
 * {@code recentWindowDays}/{@code baselineWindowDays}省略時は既定値（7日/21日）を使用する。
 */
public record TrendAnalysisRequest(Platform platform, Integer recentWindowDays, Integer baselineWindowDays) {
}
