package com.buzzanalysis.application.evaluation;

import com.buzzanalysis.application.proposal.dto.ContentProposalDto;

import java.util.List;
import java.util.Optional;

/**
 * OpenAI等のLLMを用いた「投稿内容評価」を抽象化するポート（Phase14）。実装はinfrastructure層に置く。
 * {@code referenceProposal}が空の場合は一致率を算出せずnullを返す（比較基準がない状態を0%と混同しない）。
 */
public interface AiPostEvaluationPort {

    AiEvaluationOutput evaluate(EvaluationTarget target, Optional<ContentProposalDto> referenceProposal);

    /** 評価対象の投稿内容（台本/カルーセル等から呼び出し側が正規化したフリーテキスト）。 */
    record EvaluationTarget(String title, String hookText, String structureText, String ctaText,
                             String targetAudienceText) {
    }

    record AiEvaluationOutput(Double matchRatePercent, String targetAudienceEstimate,
                               List<String> improvementSuggestions, String hookImprovement, String ctaImprovement,
                               int predictedScore) {
    }
}
