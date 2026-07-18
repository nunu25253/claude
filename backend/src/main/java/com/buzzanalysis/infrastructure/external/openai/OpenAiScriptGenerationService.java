package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.application.script.AiScriptGenerationPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * {@link AiScriptGenerationPort} のOpenAI実装。Phase10の投稿企画を元に、
 * ナレーション/テロップ/BGMイメージ/CTA/カット構成をAIに生成させる。
 * AIが返すカットの秒数範囲は尺(durationSeconds)を超える・逆転している等の不正がありうるため、
 * 呼び出し側を信頼させず本サービス内で検証・補正する（不正カットは除外、全滅時はフック20%/本編60%/CTA20%の
 * 機械的な3分割にフォールバック）。APIキー未設定時・呼び出し失敗時も同じフォールバックを使う。
 */
@Service
public class OpenAiScriptGenerationService implements AiScriptGenerationPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiScriptGenerationService.class);

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiScriptGenerationService(OpenAiClient openAiClient, OpenAiProperties properties,
                                          ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            あなたはSNSショート動画の構成作家です。与えられた投稿企画を元に、尺%d秒の動画台本を
            以下の形式のJSONオブジェクトのみで出力してください（説明文やコードブロック記法は不要）：
            {"bgmImage": "BGMの雰囲気イメージ", "callToAction": "動画全体のCTA",
             "cuts": [
               {"cutNumber": 1, "startSecond": 0, "endSecond": 5, "narration": "ナレーション",
                "telop": "テロップ文言", "visualDirection": "映像指示"}
             ]}
            cutsは時系列順に並べ、全カットの開始・終了秒は0以上%d以下に収めてください。
            """;

    @Override
    public GeneratedScript generate(ContentProposalDto proposal, int durationSeconds) {
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using rule-based fallback script generation");
            return fallbackScript(proposal, durationSeconds);
        }
        try {
            String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(durationSeconds, durationSeconds);
            String userPrompt = buildPrompt(proposal);
            String responseJson = openAiClient.chatComplete(systemPrompt, userPrompt);
            GeneratedScript parsed = parseResponse(responseJson, durationSeconds);
            if (parsed.cuts().isEmpty()) {
                return fallbackScript(proposal, durationSeconds);
            }
            return parsed;
        } catch (Exception e) {
            log.warn("OpenAI script generation failed, falling back to rule-based script: {}", e.getMessage());
            return fallbackScript(proposal, durationSeconds);
        }
    }

    private String buildPrompt(ContentProposalDto p) {
        return """
                投稿企画:
                タイトル: %s
                フック: %s
                構成: %s
                CTA: %s
                ターゲット: %s
                ジャンル: %s
                """.formatted(
                nullToDash(p.title()), nullToDash(p.hookPattern()), nullToDash(p.structureSummary()),
                nullToDash(p.callToAction()), nullToDash(p.targetAudience()), nullToDash(p.genre()));
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private GeneratedScript parseResponse(String responseJson, int durationSeconds) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        String bgmImage = textOrNull(root, "bgmImage");
        String callToAction = textOrNull(root, "callToAction");

        List<GeneratedCut> validCuts = new ArrayList<>();
        JsonNode cutsNode = root.get("cuts");
        if (cutsNode != null && cutsNode.isArray()) {
            for (JsonNode node : cutsNode) {
                GeneratedCut cut = toCut(node);
                if (isValidCut(cut, durationSeconds)) {
                    validCuts.add(cut);
                }
            }
        }
        validCuts.sort(Comparator.comparingInt(GeneratedCut::cutNumber));
        return new GeneratedScript(bgmImage, callToAction, validCuts);
    }

    private GeneratedCut toCut(JsonNode node) {
        int cutNumber = intOrDefault(node, "cutNumber", 0);
        int startSecond = intOrDefault(node, "startSecond", -1);
        int endSecond = intOrDefault(node, "endSecond", -1);
        return new GeneratedCut(cutNumber, startSecond, endSecond, textOrNull(node, "narration"),
                textOrNull(node, "telop"), textOrNull(node, "visualDirection"));
    }

    private boolean isValidCut(GeneratedCut cut, int durationSeconds) {
        return cut.startSecond() >= 0 && cut.endSecond() > cut.startSecond() && cut.endSecond() <= durationSeconds;
    }

    private int intOrDefault(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? defaultValue : value.asInt();
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    /** フック(最初20%)・本編(60%)・CTA(最後20%)の機械的な3分割フォールバック。 */
    private GeneratedScript fallbackScript(ContentProposalDto proposal, int durationSeconds) {
        int hookEnd = Math.max(1, (int) Math.round(durationSeconds * 0.2));
        int mainEnd = Math.max(hookEnd + 1, (int) Math.round(durationSeconds * 0.8));
        String note = "（ルールベース簡易生成: OpenAI未接続のため機械的に3分割）";

        List<GeneratedCut> cuts = List.of(
                new GeneratedCut(1, 0, hookEnd, nullToDash(proposal.hookPattern()),
                        nullToDash(proposal.title()), "フック導入" + note),
                new GeneratedCut(2, hookEnd, mainEnd, nullToDash(proposal.structureSummary()),
                        nullToDash(proposal.targetAudience()), "本編" + note),
                new GeneratedCut(3, mainEnd, durationSeconds, nullToDash(proposal.callToAction()),
                        nullToDash(proposal.callToAction()), "CTA" + note)
        );
        return new GeneratedScript("（未指定" + note + "）", nullToDash(proposal.callToAction()), cuts);
    }
}
