package com.buzzanalysis.domain.trend;

/**
 * トレンド分析の1項目（Phase15）。{@code baselineCount}が0件（新規出現）の場合、成長率は数値化せず
 * {@code emerging=true, growthRatePercent=null}とする（無限大を便宜的な数値で埋めない）。
 */
public record TrendItem(TrendCategory category, String value, int recentCount, int baselineCount,
                         Double growthRatePercent, boolean emerging) {
}
