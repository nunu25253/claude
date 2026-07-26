package com.buzzanalysis.application.matching.dto;

import com.buzzanalysis.application.post.dto.PostDto;

import java.util.Map;

/**
 * 一致率算出の1件分の結果（Phase6）。
 *
 * @param post             対象投稿
 * @param matchRatePercent 総合一致率（0〜100）
 * @param breakdown        Strategyごとの内訳スコア
 */
public record MatchRateResultDto(PostDto post, double matchRatePercent, Map<String, Double> breakdown) {
}
