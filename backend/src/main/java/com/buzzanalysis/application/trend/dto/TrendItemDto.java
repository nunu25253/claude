package com.buzzanalysis.application.trend.dto;

import com.buzzanalysis.domain.trend.TrendCategory;
import com.buzzanalysis.domain.trend.TrendItem;

/** {@link TrendItem}（ドメイン）のapplication層向けDTO。 */
public record TrendItemDto(TrendCategory category, String value, int recentCount, int baselineCount,
                            Double growthRatePercent, boolean emerging) {
    public static TrendItemDto from(TrendItem i) {
        return new TrendItemDto(i.category(), i.value(), i.recentCount(), i.baselineCount(),
                i.growthRatePercent(), i.emerging());
    }
}
