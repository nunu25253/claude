package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.score.BuzzScoreInput;

import java.time.OffsetDateTime;

/**
 * 投稿時間帯に基づくスコア算出。一般的にユーザーのアクティブ率が高いとされる
 * 昼休み(11-13時)・夕方〜夜(18-23時)を高評価とするヒューリスティック。
 */
public class PostTimingStrategy implements BuzzScoreStrategy {

    private static final double WEIGHT = 0.10;

    @Override
    public String name() {
        return "postTiming";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(BuzzScoreInput input) {
        OffsetDateTime publishedAt = input.post().getPublishedAt();
        if (publishedAt == null) {
            return 50.0;
        }
        int hour = publishedAt.getHour();
        if ((hour >= 11 && hour <= 13) || (hour >= 18 && hour <= 23)) {
            return 100.0;
        }
        if (hour >= 7 && hour <= 10) {
            return 70.0;
        }
        if (hour >= 14 && hour <= 17) {
            return 60.0;
        }
        // 深夜早朝(0-6時)は最も反応が薄い時間帯
        return 30.0;
    }
}
