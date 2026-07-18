package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.score.BuzzScoreInput;

import java.util.Map;

/**
 * 投稿フォーマット（リール/動画/カルーセル/画像/テキスト）に基づくスコア算出。
 * 各SNSのアルゴリズムは一般に短尺動画（リール等）を優遇する傾向があるため、フォーマット別に基礎点を与える。
 */
public class PostFormatStrategy implements BuzzScoreStrategy {

    private static final double WEIGHT = 0.10;

    private static final Map<PostType, Double> BASE_SCORES = Map.of(
            PostType.REEL, 100.0,
            PostType.VIDEO, 90.0,
            PostType.CAROUSEL, 75.0,
            PostType.IMAGE, 55.0,
            PostType.TEXT, 35.0
    );

    @Override
    public String name() {
        return "postFormat";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(BuzzScoreInput input) {
        PostType type = input.post().getPostType();
        if (type == null) {
            return 50.0;
        }
        return BASE_SCORES.getOrDefault(type, 50.0);
    }
}
