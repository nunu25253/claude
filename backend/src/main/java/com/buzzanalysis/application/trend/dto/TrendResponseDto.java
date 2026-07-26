package com.buzzanalysis.application.trend.dto;

import java.util.List;

/** {@code GET /api/v1/trends} のレスポンス全体。フロントエンドの{@code TrendResponse}型に対応する。 */
public record TrendResponseDto(List<TrendHashtagDto> hashtags, List<TrendPostDto> posts) {
}
