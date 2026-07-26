package com.buzzanalysis.domain.score.strategy;

import com.buzzanalysis.domain.score.BuzzScoreInput;

/**
 * OpenAIによる分析結果（感情スコア、バズポテンシャル推定）に基づくスコア算出。
 * AI分析がまだ行われていない場合はニュートラルな50点を返す。
 */
public class AiAnalysisStrategy implements BuzzScoreStrategy {

    private static final double WEIGHT = 0.15;

    @Override
    public String name() {
        return "aiAnalysis";
    }

    @Override
    public double weight() {
        return WEIGHT;
    }

    @Override
    public double score(BuzzScoreInput input) {
        Double sentiment = input.aiSentimentScore();
        Double viralHint = input.aiViralPotentialHint();
        if (sentiment == null && viralHint == null) {
            return 50.0;
        }
        double sentimentScore = sentiment != null ? sentiment * 100.0 : 50.0;
        double viralScore = viralHint != null ? viralHint * 100.0 : 50.0;
        double combined = (sentimentScore * 0.4) + (viralScore * 0.6);
        return Math.max(0.0, Math.min(100.0, combined));
    }
}
