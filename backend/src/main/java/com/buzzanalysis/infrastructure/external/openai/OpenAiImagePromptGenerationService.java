package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.imageprompt.AiImagePromptGenerationPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AiImagePromptGenerationPort} のOpenAI実装。短い方向性の一文（{@code visualDirection}）を
 * 画像生成AI（ChatGPT Image/DALL·E等）向けの具体的なプロンプト文字列に拡張する。
 * 要素ごとに独立して扱い、応答件数が入力件数と一致しない場合や個々の要素が生成できなかった場合は、
 * その要素だけルールベースのフォールバック（元の方向性にスタイル指定を機械的に付加）に切り替える。
 * APIキー未設定時・呼び出し失敗時は全要素をフォールバックする。
 */
@Service
public class OpenAiImagePromptGenerationService implements AiImagePromptGenerationPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiImagePromptGenerationService.class);

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiImagePromptGenerationService(OpenAiClient openAiClient, OpenAiProperties properties,
                                               ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
            あなたは画像生成AI（ChatGPT Image/DALL·E等）向けのプロンプトエンジニアです。
            与えられた短い「映像/画像の方向性」の一文それぞれを、被写体・構図・雰囲気・スタイルを
            含む具体的な英語の画像生成プロンプトに拡張してください。入力と同じ順序・同じ件数で、
            以下の形式のJSONオブジェクトのみを出力してください（説明文やコードブロック記法は不要）：
            {"prompts": ["拡張後のプロンプト1", "拡張後のプロンプト2"]}
            """;

    @Override
    public List<String> generate(List<String> visualDirections) {
        if (visualDirections.isEmpty()) {
            return List.of();
        }
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using rule-based fallback image prompts");
            return fallbackAll(visualDirections);
        }
        try {
            String userPrompt = buildPrompt(visualDirections);
            String responseJson = openAiClient.chatComplete(SYSTEM_PROMPT, userPrompt);
            List<String> parsed = parseResponse(responseJson);
            return mergeWithFallback(visualDirections, parsed);
        } catch (Exception e) {
            log.warn("OpenAI image prompt generation failed, falling back to rule-based prompts: {}", e.getMessage());
            return fallbackAll(visualDirections);
        }
    }

    private String buildPrompt(List<String> visualDirections) {
        StringBuilder sb = new StringBuilder("以下は" + visualDirections.size() + "件の方向性です。\n\n");
        for (int i = 0; i < visualDirections.size(); i++) {
            sb.append(i + 1).append(". ").append(nullToDash(visualDirections.get(i))).append("\n");
        }
        return sb.toString();
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private List<String> parseResponse(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode promptsNode = root.get("prompts");
        List<String> results = new ArrayList<>();
        if (promptsNode != null && promptsNode.isArray()) {
            for (JsonNode node : promptsNode) {
                results.add(node.isNull() ? null : node.asText());
            }
        }
        return results;
    }

    /** 応答件数が入力と一致しない/個々の要素が空の場合、その要素だけフォールバックに差し替える。 */
    private List<String> mergeWithFallback(List<String> visualDirections, List<String> parsed) {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < visualDirections.size(); i++) {
            String candidate = i < parsed.size() ? parsed.get(i) : null;
            if (candidate == null || candidate.isBlank()) {
                results.add(fallbackOne(visualDirections.get(i)));
            } else {
                results.add(candidate);
            }
        }
        return results;
    }

    private List<String> fallbackAll(List<String> visualDirections) {
        return visualDirections.stream().map(this::fallbackOne).toList();
    }

    private String fallbackOne(String visualDirection) {
        String base = nullToDash(visualDirection);
        return base + ", photorealistic, high detail, professional social media content style"
                + "（ルールベース簡易生成: OpenAI未接続または生成失敗のため方向性にスタイル指定を機械的に付加）";
    }
}
