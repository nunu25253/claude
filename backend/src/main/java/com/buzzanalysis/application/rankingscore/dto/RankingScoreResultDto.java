package com.buzzanalysis.application.rankingscore.dto;

import com.buzzanalysis.application.post.dto.PostDto;

import java.util.Map;

/**
 * 総合ランキングスコア算出の1件分の結果（Phase7）。
 *
 * @param post             対象投稿
 * @param rankingScore     総合ランキングスコア（0〜100）
 * @param matchRatePercent Phase6の一致率（参考値としてあわせて返す）
 * @param breakdown        Strategyごとの内訳スコア
 */
public record RankingScoreResultDto(PostDto post, double rankingScore, double matchRatePercent,
                                     Map<String, Double> breakdown) {
}
