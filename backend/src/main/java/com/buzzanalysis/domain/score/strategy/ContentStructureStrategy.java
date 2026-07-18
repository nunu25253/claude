package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.score.BuzzScoreInput;

/**
 * コンテンツ構成に基づくスコア算出。キャプションの長さ・改行構造や、動画の長さ／カルーセルの枚数といった
 * 「読みやすさ・消費しやすさ」に関わる構造的シグナルを評価する。
 */
public class ContentStructureStrategy implements BuzzScoreStrategy {

    private static final double WEIGHT = 0.10;

    @Override
    public String name() {
        return "contentStructure";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(BuzzScoreInput input) {
        var post = input.post();
        double score = 50.0;

        String caption = post.getCaption();
        if (caption != null && !caption.isBlank()) {
            int length = caption.length();
            // 100〜400文字程度が読みやすく最適とみなす
            if (length >= 100 && length <= 400) {
                score += 20;
            } else if (length > 0 && length < 100) {
                score += 5;
            } else if (length > 400) {
                score -= 5;
            }
            if (caption.contains("\n")) {
                score += 10; // 改行があり構造化されている
            }
            if (caption.contains("?") || caption.contains("？")) {
                score += 5; // 問いかけがあるとコメントを誘発しやすい
            }
        }

        PostType type = post.getPostType();
        if (type == PostType.VIDEO || type == PostType.REEL) {
            Integer duration = post.getVideoDurationSeconds();
            if (duration != null) {
                // 15〜60秒程度が最後まで視聴されやすい長さ
                if (duration >= 15 && duration <= 60) {
                    score += 15;
                } else if (duration > 60 && duration <= 180) {
                    score += 5;
                }
            }
        } else if (type == PostType.CAROUSEL) {
            Integer imageCount = post.getImageCount();
            if (imageCount != null && imageCount >= 3 && imageCount <= 7) {
                score += 15; // スワイプを継続させやすい枚数
            }
        }

        return Math.max(0.0, Math.min(100.0, score));
    }
}
