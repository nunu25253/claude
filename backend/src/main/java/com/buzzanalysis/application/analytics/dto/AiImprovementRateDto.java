package com.buzzanalysis.application.analytics.dto;

/**
 * 「AI提案の的中率」レスポンス。初回分析→AIの改善提案→再分析、というサイクルを経た投稿群のうち、
 * 実際にBuzzScoreが向上した割合を返す。サンプルが無い場合はimprovedPercentage/averageScoreDelta
 * がnullになる(架空の数値を表示しないため)。
 *
 * @param sampleSize           集計対象(2回以上再分析された投稿)の件数
 * @param improvedPercentage   初回analyzeより最新のBuzzScoreが高かった投稿の割合(0〜100、サンプル無しならnull)
 * @param averageScoreDelta    初回から最新までのBuzzScore変化量の平均(サンプル無しならnull)
 */
public record AiImprovementRateDto(int sampleSize, Double improvedPercentage, Double averageScoreDelta) {
}
