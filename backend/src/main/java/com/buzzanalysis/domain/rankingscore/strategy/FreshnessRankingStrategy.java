package com.buzzanalysis.domain.rankingscore.strategy;

import com.buzzanalysis.domain.rankingscore.RankingScoreInput;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * 投稿からの経過時間に基づく指数減衰スコア（新規、Phase7）。投稿直後は100点に近く、
 * 半減期({@link #HALF_LIFE_DAYS}日)ごとにスコアが半分になる。減衰定数は暫定値であり、
 * 実データでの効果検証が今後必要（docs/phases/phase7_ranking.md 参照）。
 */
public class FreshnessRankingStrategy implements RankingScoreStrategy {

    private static final double WEIGHT = 0.15;
    private static final double HALF_LIFE_DAYS = 14.0;

    @Override
    public String name() {
        return "freshness";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(RankingScoreInput input) {
        OffsetDateTime publishedAt = input.post().getPublishedAt();
        if (publishedAt == null) {
            return 50.0;
        }
        double ageDays = Math.max(0.0, Duration.between(publishedAt, OffsetDateTime.now()).toHours() / 24.0);
        double decay = Math.pow(0.5, ageDays / HALF_LIFE_DAYS);
        return 100.0 * decay;
    }
}
