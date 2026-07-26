package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.score.BuzzScoreInput;

/**
 * コメント率（コメント数 / いいね数）に基づくスコア算出。
 * コメントが多い投稿は議論やシェアを誘発しやすく「バズり」の強いシグナルとなる。
 */
public class CommentRateStrategy implements BuzzScoreStrategy {

    private static final double WEIGHT = 0.10;
    private static final double SATURATION_RATE = 0.08;

    @Override
    public String name() {
        return "commentRate";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(BuzzScoreInput input) {
        Long likes = input.post().getLikeCount();
        Long comments = input.post().getCommentCount();
        if (likes == null || likes <= 0 || comments == null) {
            return 0.0;
        }
        double rate = comments / (double) likes;
        double score = (rate / SATURATION_RATE) * 100.0;
        return Math.max(0.0, Math.min(100.0, score));
    }
}
