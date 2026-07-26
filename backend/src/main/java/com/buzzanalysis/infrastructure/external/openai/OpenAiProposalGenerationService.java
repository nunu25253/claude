package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import com.buzzanalysis.application.proposal.AiProposalGenerationPort;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AiProposalGenerationPort} のOpenAI実装。Phase8の共通点分析結果を元に、
 * {@code {"proposals": [...]}}形式のJSONオブジェクトとして複数件の企画をAIに生成させる。
 * 要求件数を超えた分は切り詰め、必須項目（title）が欠けたproposalはスキップする。
 * APIキー未設定時、またはAPI呼び出しに失敗した場合は、共通パターンの文言をそのまま使った
 * ルールベースのフォールバック企画を返す。
 */
@Service
public class OpenAiProposalGenerationService implements AiProposalGenerationPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiProposalGenerationService.class);

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiProposalGenerationService(OpenAiClient openAiClient, OpenAiProperties properties,
                                            ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            あなたはSNSのバズ投稿企画を専門とするプランナーです。与えられた「共通パターン」情報を元に、
            新しい投稿企画を必ず%d件、以下の形式のJSONオブジェクトのみで出力してください（説明文やコードブロック記法は不要）：
            {"proposals": [
              {"title": "投稿タイトル案", "hookPattern": "冒頭フック案", "structureSummary": "投稿構成案",
               "callToAction": "CTA案", "targetAudience": "想定ターゲット", "genre": "ジャンル",
               "recommendedFormat": "SHORT_VIDEO|LONG_VIDEO|SINGLE_IMAGE|MULTI_IMAGE_CAROUSEL|TEXT_ONLYのいずれか",
               "reasoning": "この企画が共通パターンに基づき有効だと考える理由"}
            ]}
            各企画は互いに異なる切り口にしてください。
            """;

    @Override
    public List<GeneratedProposal> generate(CommonalityAnalysisResultDto commonality, int count) {
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using rule-based fallback proposal generation");
            return fallbackProposals(commonality, count);
        }
        try {
            String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(count);
            String userPrompt = buildPrompt(commonality);
            String responseJson = openAiClient.chatComplete(systemPrompt, userPrompt);
            List<GeneratedProposal> parsed = parseResponse(responseJson);
            if (parsed.isEmpty()) {
                return fallbackProposals(commonality, count);
            }
            return parsed.size() > count ? parsed.subList(0, count) : parsed;
        } catch (Exception e) {
            log.warn("OpenAI proposal generation failed, falling back to rule-based proposals: {}", e.getMessage());
            return fallbackProposals(commonality, count);
        }
    }

    private String buildPrompt(CommonalityAnalysisResultDto c) {
        return """
                共通パターン情報:
                共通タイトル傾向: %s
                共通フック傾向: %s
                共通CTA傾向: %s
                共通構成傾向: %s
                共通ターゲット傾向: %s
                共通ハッシュタグ: %s
                共通コンテンツ形式: %s
                対象投稿数: %d件
                """.formatted(
                nullToDash(c.commonTitlePattern()), nullToDash(c.commonHookPattern()),
                nullToDash(c.commonCtaPattern()), nullToDash(c.commonStructurePattern()),
                nullToDash(c.commonTargetPattern()),
                c.commonHashtags() == null || c.commonHashtags().isEmpty() ? "-" : String.join(", ", c.commonHashtags()),
                c.commonContentFormat() == null ? "-" : c.commonContentFormat().name(),
                c.totalPostCount());
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private List<GeneratedProposal> parseResponse(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode proposalsNode = root.get("proposals");
        List<GeneratedProposal> results = new ArrayList<>();
        if (proposalsNode == null || !proposalsNode.isArray()) {
            return results;
        }
        for (JsonNode node : proposalsNode) {
            String title = textOrNull(node, "title");
            if (title == null || title.isBlank()) {
                continue;
            }
            results.add(new GeneratedProposal(
                    title,
                    textOrNull(node, "hookPattern"),
                    textOrNull(node, "structureSummary"),
                    textOrNull(node, "callToAction"),
                    textOrNull(node, "targetAudience"),
                    textOrNull(node, "genre"),
                    parseFormat(textOrNull(node, "recommendedFormat")),
                    textOrNull(node, "reasoning")
            ));
        }
        return results;
    }

    private ContentFormat parseFormat(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ContentFormat.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private List<GeneratedProposal> fallbackProposals(CommonalityAnalysisResultDto c, int count) {
        List<GeneratedProposal> results = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            results.add(new GeneratedProposal(
                    "企画案" + i + "（" + nullToDash(c.commonTitlePattern()) + "を応用）",
                    nullToDash(c.commonHookPattern()),
                    nullToDash(c.commonStructurePattern()),
                    nullToDash(c.commonCtaPattern()),
                    nullToDash(c.commonTargetPattern()),
                    null,
                    c.commonContentFormat(),
                    "（ルールベース簡易生成: OpenAI未接続のため共通パターンをそのまま適用）"
            ));
        }
        return results;
    }
}
