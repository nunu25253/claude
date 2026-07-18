package com.buzzanalysis.application.competitor.dto;

/**
 * 競合比較の結果（Phase9）。
 *
 * @param target        自社（分析対象）アカウントの統計
 * @param competitor    比較対象（競合）アカウントの統計
 * @param aiExplanation AIによる差分の自然言語説明
 */
public record CompetitorComparisonResultDto(CompetitorStatsDto target, CompetitorStatsDto competitor,
                                             String aiExplanation) {
}
