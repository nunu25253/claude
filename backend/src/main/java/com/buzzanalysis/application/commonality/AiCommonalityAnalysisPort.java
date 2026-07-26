package com.buzzanalysis.application.commonality;

import java.util.List;

/**
 * OpenAI等のLLMを用いた「共通点分析」を抽象化するポート（Phase8）。実装はinfrastructure層に置く。
 * トークン数・コスト抑制のため、呼び出し側でスニペット要約・サンプリング済みのリストを渡す前提とする。
 */
public interface AiCommonalityAnalysisPort {

    AiCommonalityOutput analyze(List<PostSnippet> snippets);

    /** 共通点分析用に要約された1投稿分のスニペット（各項目は最大80文字程度に切り詰め済み）。 */
    record PostSnippet(String titleSnippet, String hookSnippet, String ctaSnippet, String structureSnippet,
                        String targetSnippet) {
    }

    record AiCommonalityOutput(String commonTitlePattern, String commonHookPattern, String commonCtaPattern,
                                String commonStructurePattern, String commonTargetPattern) {
    }
}
