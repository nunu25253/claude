package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.evaluation.AiPostEvaluationPort;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link AiPostEvaluationPort} のOpenAI実装。投稿内容（タイトル/フック/構成/CTA/ターゲット）を
 * 評価し、一致率（企画が指定された場合のみ）・想定ターゲット・改善提案・フック/CTA改善案・
 * 予測投稿スコア(0〜100)をAIが生成する。予測投稿スコアはPhase7のランキングスコア（実測値ベース）
 * とは算出根拠が異なる定性的な予測値である（設計doc参照）。
 * APIキー未設定時・呼び出し失敗時は、各項目の文字数・空欄有無に基づく簡易ヒューリスティックで
 * フォールバックする。
 */
@Service
public class OpenAiPostEvaluationService implements AiPostEvaluationPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiPostEvaluationService.class);

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiPostEvaluationService(OpenAiClient openAiClient, OpenAiProperties properties,
                                        ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT_WITH_PROPOSAL = """
            あなたはSNS投稿の編集者です。ユーザーが作成した投稿内容を、参考として与えられた元企画と
            比較して評価してください。以下の形式のJSONオブジェクトのみを出力してください
            （説明文やコードブロック記法は不要）：
            {"matchRatePercent": 0から100の数値（元企画とどれだけ一致しているか）,
             "targetAudienceEstimate": "想定ターゲットの推定", "improvementSuggestions": ["改善提案1", "改善提案2"],
             "hookImprovement": "フックの改善案", "ctaImprovement": "CTAの改善案",
             "predictedScore": 0から100の整数（フック・構成・CTA・ターゲットの明確さ等から見積もる予測投稿スコア）}
            """;

    private static final String SYSTEM_PROMPT_WITHOUT_PROPOSAL = """
            あなたはSNS投稿の編集者です。ユーザーが作成した投稿内容を単体で評価してください
            （比較対象の元企画はありません）。以下の形式のJSONオブジェクトのみを出力してください
            （説明文やコードブロック記法は不要）：
            {"targetAudienceEstimate": "想定ターゲットの推定", "improvementSuggestions": ["改善提案1", "改善提案2"],
             "hookImprovement": "フックの改善案", "ctaImprovement": "CTAの改善案",
             "predictedScore": 0から100の整数（フック・構成・CTA・ターゲットの明確さ等から見積もる予測投稿スコア）}
            matchRatePercentは出力不要です（元企画がないため算出できません）。
            """;

    @Override
    public AiEvaluationOutput evaluate(EvaluationTarget target, Optional<ContentProposalDto> referenceProposal) {
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using heuristic fallback post evaluation");
            return fallbackEvaluation(target, referenceProposal);
        }
        try {
            String systemPrompt = referenceProposal.isPresent() ? SYSTEM_PROMPT_WITH_PROPOSAL : SYSTEM_PROMPT_WITHOUT_PROPOSAL;
            String userPrompt = buildPrompt(target, referenceProposal);
            String responseJson = openAiClient.chatComplete(systemPrompt, userPrompt);
            return parseResponse(responseJson, referenceProposal.isPresent());
        } catch (Exception e) {
            log.warn("OpenAI post evaluation failed, falling back to heuristic evaluation: {}", e.getMessage());
            return fallbackEvaluation(target, referenceProposal);
        }
    }

    private String buildPrompt(EvaluationTarget t, Optional<ContentProposalDto> referenceProposal) {
        StringBuilder sb = new StringBuilder("評価対象の投稿内容:\n");
        sb.append("タイトル: ").append(nullToDash(t.title())).append("\n");
        sb.append("フック: ").append(nullToDash(t.hookText())).append("\n");
        sb.append("構成: ").append(nullToDash(t.structureText())).append("\n");
        sb.append("CTA: ").append(nullToDash(t.ctaText())).append("\n");
        sb.append("ターゲット: ").append(nullToDash(t.targetAudienceText())).append("\n");
        referenceProposal.ifPresent(p -> sb.append("\n元企画:\n")
                .append("タイトル: ").append(nullToDash(p.title())).append("\n")
                .append("フック: ").append(nullToDash(p.hookPattern())).append("\n")
                .append("構成: ").append(nullToDash(p.structureSummary())).append("\n")
                .append("CTA: ").append(nullToDash(p.callToAction())).append("\n")
                .append("ターゲット: ").append(nullToDash(p.targetAudience())).append("\n"));
        return sb.toString();
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private AiEvaluationOutput parseResponse(String responseJson, boolean hasReferenceProposal) throws Exception {
        JsonNode node = objectMapper.readTree(responseJson);
        Double matchRatePercent = hasReferenceProposal ? doubleOrNull(node, "matchRatePercent") : null;
        List<String> suggestions = new ArrayList<>();
        JsonNode suggestionsNode = node.get("improvementSuggestions");
        if (suggestionsNode != null && suggestionsNode.isArray()) {
            for (JsonNode s : suggestionsNode) {
                if (!s.isNull() && !s.asText().isBlank()) {
                    suggestions.add(s.asText());
                }
            }
        }
        return new AiEvaluationOutput(
                matchRatePercent,
                textOrDefault(node, "targetAudienceEstimate"),
                suggestions,
                textOrDefault(node, "hookImprovement"),
                textOrDefault(node, "ctaImprovement"),
                clampScore(intOrDefault(node, "predictedScore", 50))
        );
    }

    private Double doubleOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? null : value.asDouble();
    }

    private int intOrDefault(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? defaultValue : value.asInt();
    }

    private String textOrDefault(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    /** OpenAI未接続・失敗時の簡易ヒューリスティック評価。各項目の空欄有無・文字数から見積もる。 */
    private AiEvaluationOutput fallbackEvaluation(EvaluationTarget t, Optional<ContentProposalDto> referenceProposal) {
        String note = "（ヒューリスティック簡易評価: OpenAI未接続または評価失敗のため参考値）";
        int filledFieldCount = countFilled(t.title(), t.hookText(), t.structureText(), t.ctaText(),
                t.targetAudienceText());
        int predictedScore = clampScore(filledFieldCount * 20);

        Double matchRatePercent = null;
        if (referenceProposal.isPresent()) {
            ContentProposalDto p = referenceProposal.get();
            int matchedFieldCount = 0;
            int comparableFieldCount = 0;
            matchedFieldCount += fieldRoughlyMatches(t.hookText(), p.hookPattern()) ? 1 : 0;
            comparableFieldCount += p.hookPattern() != null ? 1 : 0;
            matchedFieldCount += fieldRoughlyMatches(t.structureText(), p.structureSummary()) ? 1 : 0;
            comparableFieldCount += p.structureSummary() != null ? 1 : 0;
            matchedFieldCount += fieldRoughlyMatches(t.ctaText(), p.callToAction()) ? 1 : 0;
            comparableFieldCount += p.callToAction() != null ? 1 : 0;
            matchRatePercent = comparableFieldCount == 0 ? null : (matchedFieldCount * 100.0 / comparableFieldCount);
        }

        List<String> suggestions = List.of("フック・構成・CTAの各項目を具体的に記述してください" + note);
        return new AiEvaluationOutput(matchRatePercent, "（未算出" + note + "）", suggestions,
                "フックをより具体的にしてください" + note, "CTAをより明確にしてください" + note, predictedScore);
    }

    private int countFilled(String... values) {
        int count = 0;
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                count++;
            }
        }
        return count;
    }

    private boolean fieldRoughlyMatches(String actual, String expected) {
        if (actual == null || expected == null || actual.isBlank() || expected.isBlank()) {
            return false;
        }
        return actual.contains(expected) || expected.contains(actual);
    }
}
