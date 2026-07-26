package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.commonality.AiCommonalityAnalysisPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link AiCommonalityAnalysisPort} のOpenAI実装。要約済みスニペットのリストから、
 * 共通タイトル/フック/CTA/構成/ターゲットのパターンをJSON形式で抽出する。
 * APIキー未設定時、またはAPI呼び出しに失敗した場合は、スニペットの単純な頻出語に基づく
 * ルールベースのフォールバックを返す。
 */
@Service
public class OpenAiCommonalityAnalysisService implements AiCommonalityAnalysisPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCommonalityAnalysisService.class);

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiCommonalityAnalysisService(OpenAiClient openAiClient, OpenAiProperties properties,
                                             ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
            あなたはSNSのバズ分析専門家です。複数の投稿の要約情報（タイトル/フック/CTA/構成/ターゲットの
            各分析スニペット）が与えられます。これらに共通するパターン・傾向を発見し、以下のキーを持つ
            JSONオブジェクトのみを出力してください（説明文やコードブロック記法は不要）：
            commonTitlePattern, commonHookPattern, commonCtaPattern, commonStructurePattern, commonTargetPattern
            """;

    @Override
    public AiCommonalityOutput analyze(List<PostSnippet> snippets) {
        if (snippets.isEmpty()) {
            return noDataOutput();
        }
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using rule-based fallback commonality analysis");
            return fallbackAnalysis(snippets);
        }
        try {
            String userPrompt = buildPrompt(snippets);
            String responseJson = openAiClient.chatComplete(SYSTEM_PROMPT, userPrompt);
            return parseResponse(responseJson);
        } catch (Exception e) {
            log.warn("OpenAI commonality analysis failed, falling back to rule-based analysis: {}", e.getMessage());
            return fallbackAnalysis(snippets);
        }
    }

    private String buildPrompt(List<PostSnippet> snippets) {
        StringBuilder sb = new StringBuilder("以下は" + snippets.size() + "件の投稿の分析スニペットです。\n\n");
        for (int i = 0; i < snippets.size(); i++) {
            PostSnippet s = snippets.get(i);
            sb.append("投稿").append(i + 1).append(": ")
                    .append("タイトル=").append(nullToDash(s.titleSnippet())).append(", ")
                    .append("フック=").append(nullToDash(s.hookSnippet())).append(", ")
                    .append("CTA=").append(nullToDash(s.ctaSnippet())).append(", ")
                    .append("構成=").append(nullToDash(s.structureSnippet())).append(", ")
                    .append("ターゲット=").append(nullToDash(s.targetSnippet()))
                    .append("\n");
        }
        return sb.toString();
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private AiCommonalityOutput parseResponse(String responseJson) throws Exception {
        JsonNode node = objectMapper.readTree(responseJson);
        return new AiCommonalityOutput(
                textOrDefault(node, "commonTitlePattern"),
                textOrDefault(node, "commonHookPattern"),
                textOrDefault(node, "commonCtaPattern"),
                textOrDefault(node, "commonStructurePattern"),
                textOrDefault(node, "commonTargetPattern")
        );
    }

    private String textOrDefault(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private AiCommonalityOutput fallbackAnalysis(List<PostSnippet> snippets) {
        String note = "（ルールベース簡易分析: OpenAI未接続のため" + snippets.size() + "件のスニペットから代表例を提示）";
        return new AiCommonalityOutput(
                "代表例: " + firstNonBlank(snippets, PostSnippet::titleSnippet) + note,
                "代表例: " + firstNonBlank(snippets, PostSnippet::hookSnippet) + note,
                "代表例: " + firstNonBlank(snippets, PostSnippet::ctaSnippet) + note,
                "代表例: " + firstNonBlank(snippets, PostSnippet::structureSnippet) + note,
                "代表例: " + firstNonBlank(snippets, PostSnippet::targetSnippet) + note
        );
    }

    private String firstNonBlank(List<PostSnippet> snippets, java.util.function.Function<PostSnippet, String> getter) {
        return snippets.stream().map(getter).filter(v -> v != null && !v.isBlank()).findFirst().orElse("該当データなし");
    }

    private AiCommonalityOutput noDataOutput() {
        String message = "分析結果が存在する投稿がないため共通パターンを抽出できません";
        return new AiCommonalityOutput(message, message, message, message, message);
    }
}
