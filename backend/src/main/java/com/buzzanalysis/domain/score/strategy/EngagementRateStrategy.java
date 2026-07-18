package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.score.BuzzScoreInput;

/**
 * エンゲージメント率（いいね数+コメント数 / 再生数）に基づくスコア算出。
 * 再生数が取得できない投稿（画像投稿等）ではフォロワー数の代わりに一定のベースラインで評価する。
 */
public class EngagementRateStrategy implements BuzzScoreStrategy {

    private static final double WEIGHT = 0.20;
    /** エンゲージメント率がこの値以上で満点(100点)とみなす閾値。一般的なSNS平均(数%)より高めに設定。 */
    private static final double SATURATION_RATE = 0.15;

    @Override
    public String name() {
        return "engagementRate";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(BuzzScoreInput input) {
        Post post = input.post();
        long likes = nvl(post.getLikeCount());
        long comments = nvl(post.getCommentCount());
        long engagements = likes + comments;
        Long views = post.getViewCount();

        double rate;
        if (views != null && views > 0) {
            rate = engagements / (double) views;
        } else if (likes > 0) {
            // 再生数が無い場合は「コメント/いいね」の比率を疑似エンゲージメント率として使う
            rate = comments / (double) likes;
        } else {
            return 0.0;
        }
        double score = (rate / SATURATION_RATE) * 100.0;
        return clamp(score);
    }

    private long nvl(Long value) {
        return value == null ? 0L : value;
    }

    private double clamp(double v) {
        return Math.max(0.0, Math.min(100.0, v));
    }
}
