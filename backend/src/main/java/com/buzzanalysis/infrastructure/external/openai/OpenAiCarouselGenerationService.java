package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.carousel.AiCarouselGenerationPort;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AiCarouselGenerationPort} のOpenAI実装。Phase10の投稿企画を元に、Instagramカルーセル
 * （2〜8ページ）の各ページ内容（見出し/本文/画像方向性）をAIに生成させる。ページの役割判定はAIに
 * 求めない（呼び出し元のapplication層が配列内の位置から決定する）。
 * AI応答のページ数が2〜8の範囲外の場合は切り詰め、2件未満（実質破損）の場合はフォールバックする。
 * APIキー未設定時・呼び出し失敗時も同じフォールバックを使う。
 */
@Service
public class OpenAiCarouselGenerationService implements AiCarouselGenerationPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCarouselGenerationService.class);
    private static final int MIN_PAGES = 2;
    private static final int MAX_PAGES = 8;

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiCarouselGenerationService(OpenAiClient openAiClient, OpenAiProperties properties,
                                            ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    private static final String SYSTEM_PROMPT = """
            あなたはSNSのカルーセル投稿構成作家です。与えられた投稿企画を元に、Instagramカルーセル
            (2〜8ページ)の各ページ内容を、以下の形式のJSONオブジェクトのみで出力してください
            （説明文やコードブロック記法は不要）：
            {"pages": [
              {"headline": "見出し", "bodyText": "本文", "visualDirection": "画像の方向性"}
            ]}
            1ページ目は読者の興味を引くフック、最終ページは行動喚起(CTA)を意識した内容にしてください
            （役割ラベル自体は出力不要です）。中間ページに完結したCTAを書かないでください。
            """;

    @Override
    public GeneratedCarousel generate(ContentProposalDto proposal) {
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using rule-based fallback carousel generation");
            return fallbackCarousel(proposal);
        }
        try {
            String userPrompt = buildPrompt(proposal);
            String responseJson = openAiClient.chatComplete(SYSTEM_PROMPT, userPrompt);
            GeneratedCarousel parsed = parseResponse(responseJson);
            if (parsed.pages().size() < MIN_PAGES) {
                return fallbackCarousel(proposal);
            }
            return parsed;
        } catch (Exception e) {
            log.warn("OpenAI carousel generation failed, falling back to rule-based carousel: {}", e.getMessage());
            return fallbackCarousel(proposal);
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

    private GeneratedCarousel parseResponse(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode pagesNode = root.get("pages");
        List<GeneratedPage> pages = new ArrayList<>();
        if (pagesNode != null && pagesNode.isArray()) {
            for (JsonNode node : pagesNode) {
                if (pages.size() >= MAX_PAGES) {
                    break;
                }
                String headline = textOrNull(node, "headline");
                if (headline == null || headline.isBlank()) {
                    continue;
                }
                pages.add(new GeneratedPage(headline, textOrNull(node, "bodyText"),
                        textOrNull(node, "visualDirection")));
            }
        }
        return new GeneratedCarousel(pages);
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    /** 企画のフック/構成/CTAをそのまま使った3ページ構成のフォールバック。 */
    private GeneratedCarousel fallbackCarousel(ContentProposalDto proposal) {
        String note = "（ルールベース簡易生成: OpenAI未接続または生成失敗のため企画内容をそのまま適用）";
        List<GeneratedPage> pages = List.of(
                new GeneratedPage(nullToDash(proposal.title()), nullToDash(proposal.hookPattern()), "フック" + note),
                new GeneratedPage(nullToDash(proposal.structureSummary()), nullToDash(proposal.structureSummary()),
                        "説明" + note),
                new GeneratedPage(nullToDash(proposal.callToAction()), nullToDash(proposal.callToAction()),
                        "CTA" + note)
        );
        return new GeneratedCarousel(pages);
    }
}
