package com.buzzanalysis.application.post.dto;

/**
 * 投稿分析結果の相対評価(戦略監査レポート4章: 前回投稿比・同ジャンル平均比)。
 * 比較対象が存在しない場合(初回投稿/ジャンル未判定等)は該当フィールドがnullになる。
 *
 * @param previousPostScore 同一アカウントの直前の投稿のBuzzScore(存在しない場合null)
 * @param genreAverageScore 同一プラットフォーム・同一ジャンルの投稿群の平均BuzzScore(算出不能な場合null)
 * @param genreSampleSize   genreAverageScoreの算出に使ったサンプル数(genreAverageScoreがnullの場合0)
 */
public record PostScoreComparisonDto(Double previousPostScore, Double genreAverageScore, int genreSampleSize) {
}
