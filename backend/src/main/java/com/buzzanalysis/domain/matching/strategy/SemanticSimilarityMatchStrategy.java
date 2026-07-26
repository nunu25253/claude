package com.buzzanalysis.domain.matching.strategy;

import com.buzzanalysis.domain.matching.MatchRateInput;

/**
 * キーワード/商品名/ブランド/ASP案件名の合成テキストと投稿本文のEmbeddingコサイン類似度(Phase4)に基づくスコア。
 * 自由文条件が1つも指定されていない場合、またはEmbedding未生成で類似度が計算できない場合は中立スコアを返す。
 */
public class SemanticSimilarityMatchStrategy implements MatchRateStrategy {

    private static final double WEIGHT = 0.30;
    private static final double NEUTRAL_SCORE = 50.0;

    @Override
    public String name() {
        return "semanticSimilarity";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(MatchRateInput input) {
        if (input.condition().toSemanticQueryText().isBlank() || input.semanticSimilarity() == null) {
            return NEUTRAL_SCORE;
        }
        return input.semanticSimilarity() * 100.0;
    }
}
