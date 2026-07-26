package com.buzzanalysis.domain.score;

import com.buzzanalysis.domain.post.Post;

/**
 * BuzzScore計算に必要な入力をまとめた値オブジェクト。
 * AI分析結果はオプション（分析前でもフォーマット/ハッシュタグ等のスコアは算出できるようにするため）。
 *
 * @param post                 スコア算出対象の投稿
 * @param aiSentimentScore     AIによる感情スコア（0.0〜1.0、ポジティブなほど高い）。未算出ならnull
 * @param aiViralPotentialHint AIが推定したバズポテンシャル（0.0〜1.0）。未算出ならnull
 */
public record BuzzScoreInput(Post post, Double aiSentimentScore, Double aiViralPotentialHint) {

    public static BuzzScoreInput withoutAi(Post post) {
        return new BuzzScoreInput(post, null, null);
    }
}
