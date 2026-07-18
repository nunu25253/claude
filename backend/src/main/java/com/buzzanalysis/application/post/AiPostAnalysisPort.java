package com.buzzanalysis.application.post;

import com.buzzanalysis.domain.post.Post;

/**
 * OpenAI等のLLMを用いた投稿分析を抽象化するポート。実装（WebClientでOpenAI APIを呼ぶ）はinfrastructure層に置く。
 * APIキー未設定時はinfrastructure実装側でルールベースのフォールバック分析を返すこと。
 */
public interface AiPostAnalysisPort {

    /**
     * 投稿内容からAI分析結果を生成する。
     */
    AiAnalysisOutput analyze(Post post);

    /**
     * AI分析出力。感情スコア・バズポテンシャルはBuzzScore算出のAiAnalysisStrategyでも利用する。
     *
     * @param sentimentScore     0.0(ネガティブ)〜1.0(ポジティブ)
     * @param viralPotentialHint 0.0〜1.0でAIが推定したバズりやすさ
     */
    record AiAnalysisOutput(
            String whyItWentViral,
            String targetAudience,
            String hook,
            String callToAction,
            String sentimentAnalysis,
            double sentimentScore,
            String videoStructureAnalysis,
            String carouselStructureAnalysis,
            String titleAnalysis,
            String textAnalysis,
            String postingTimeAnalysis,
            String hashtagAnalysis,
            String improvementSuggestions,
            double viralPotentialHint
    ) {
    }
}
