package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.score.BuzzScoreInput;

import java.util.List;

/**
 * ハッシュタグ数に基づくスコア算出。少なすぎても発見されにくく、多すぎるとスパム的に見なされ
 * リーチが下がる傾向があるため、3〜8個程度を最適レンジとした山型のスコアリングを行う。
 */
public class HashtagStrategy implements BuzzScoreStrategy {

    private static final double WEIGHT = 0.10;
    private static final int OPTIMAL_MIN = 3;
    private static final int OPTIMAL_MAX = 8;

    @Override
    public String name() {
        return "hashtag";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(BuzzScoreInput input) {
        List<String> hashtags = input.post().getHashtags();
        int count = hashtags == null ? 0 : hashtags.size();
        if (count == 0) {
            return 20.0;
        }
        if (count >= OPTIMAL_MIN && count <= OPTIMAL_MAX) {
            return 100.0;
        }
        if (count < OPTIMAL_MIN) {
            return 20.0 + (80.0 * count / OPTIMAL_MIN);
        }
        // 多すぎる場合は超過分に応じて減点（30個で0点に近づく）
        int excess = count - OPTIMAL_MAX;
        double penalty = excess * 5.0;
        return Math.max(10.0, 100.0 - penalty);
    }
}
