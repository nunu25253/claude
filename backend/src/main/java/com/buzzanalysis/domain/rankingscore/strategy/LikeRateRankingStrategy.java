package com.buzzanalysis.domain.rankingscore.strategy;

import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.rankingscore.RankingScoreInput;

/**
 * いいね数 / 再生数の比率に基づくスコア（新規、Phase7）。既存の{@code EngagementRateStrategy}は
 * いいね+コメントの合算比率のため、いいね単体の比率は本Strategyで初めて評価する。
 */
public class LikeRateRankingStrategy implements RankingScoreStrategy {

    private static final double WEIGHT = 0.15;
    /** いいね率がこの値以上で満点(100点)とみなす閾値。 */
    private static final double SATURATION_RATE = 0.10;

    @Override
    public String name() {
        return "likeRate";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(RankingScoreInput input) {
        Post post = input.post();
        long likes = post.getLikeCount() == null ? 0L : post.getLikeCount();
        Long views = post.getViewCount();
        if (views == null || views <= 0) {
            return 30.0; // 再生数が未計測の投稿はいいね率を算出できないため保守的なスコア
        }
        double rate = likes / (double) views;
        return Math.max(0.0, Math.min(100.0, (rate / SATURATION_RATE) * 100.0));
    }
}
