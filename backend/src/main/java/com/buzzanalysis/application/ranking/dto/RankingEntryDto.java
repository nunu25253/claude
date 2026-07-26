package com.buzzanalysis.application.ranking.dto;

import com.buzzanalysis.application.post.dto.PostDto;

/** ランキング1件分のDTO（順位・スコア・投稿情報）。 */
public record RankingEntryDto(int rankPosition, double score, PostDto post) {
}
