package com.buzzanalysis.application.ranking.dto;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.ranking.RankingType;

/** ランキング取得ユースケースの入力クエリ。 */
public record RankingQuery(RankingType type, String genre, Platform platform, int limit) {
}
