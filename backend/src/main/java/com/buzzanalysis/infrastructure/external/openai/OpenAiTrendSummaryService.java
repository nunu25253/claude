package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.trend.AiTrendSummaryPort;
import com.buzzanalysis.domain.trend.TrendItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link AiTrendSummaryPort} のOpenAI実装。{@code TrendAnalysisApplicationService}が決定的に
 * 検出したトレンド項目一覧を、自然言語のサマリー文章に変換する（トレンド検出そのものはAIに依存しない）。
 * APIキー未設定時・呼び出し失敗時は、項目を機械的に箇条書き整形したテキストにフォールバックする。
 */
@Service
public class OpenAiTrendSummaryService implements AiTrendSummaryPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiTrendSummaryService.class);

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;

    public OpenAiTrendSummaryService(OpenAiClient openAiClient, OpenAiProperties properties) {
        this.openAiClient = openAiClient;
        this.properties = properties;
    }

    private static final String SYSTEM_PROMPT = """
            あなたはSNSトレンド分析の専門家です。以下は統計的に検出された急上昇項目（ハッシュタグ/
            ジャンル/コンテンツ形式）のリストです。マーケティング担当者向けに、要点を簡潔な日本語の
            文章（3〜5文程度）で要約してください。数値の再解釈や誇張はせず、与えられたデータの範囲で
            説明してください。
            """;

    @Override
    public String summarize(List<TrendItem> items) {
        if (items.isEmpty()) {
            return "直近期間で最小サンプル数の閾値を超えるトレンド項目は検出されませんでした。";
        }
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using rule-based fallback trend summary");
            return fallbackSummary(items);
        }
        try {
            String userPrompt = buildPrompt(items);
            return openAiClient.chatCompleteAsPlainText(SYSTEM_PROMPT, userPrompt);
        } catch (Exception e) {
            log.warn("OpenAI trend summary failed, falling back to rule-based summary: {}", e.getMessage());
            return fallbackSummary(items);
        }
    }

    private String buildPrompt(List<TrendItem> items) {
        StringBuilder sb = new StringBuilder("急上昇項目一覧:\n");
        for (TrendItem item : items) {
            sb.append("- [").append(item.category()).append("] ").append(item.value())
                    .append(": 直近").append(item.recentCount()).append("件");
            if (item.emerging()) {
                sb.append("（新規急伸、ベースライン期間は0件）");
            } else {
                sb.append("、ベースライン").append(item.baselineCount()).append("件")
                        .append("、成長率").append(String.format("%.1f", item.growthRatePercent())).append("%");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String fallbackSummary(List<TrendItem> items) {
        StringBuilder sb = new StringBuilder("（ルールベース簡易要約: OpenAI未接続のため検出項目を機械的に整形）\n");
        for (TrendItem item : items) {
            sb.append("- [").append(item.category()).append("] ").append(item.value());
            if (item.emerging()) {
                sb.append(": 新規急伸（直近").append(item.recentCount()).append("件）");
            } else {
                sb.append(": 成長率").append(String.format("%.1f", item.growthRatePercent())).append("%")
                        .append("（直近").append(item.recentCount()).append("件 / ベースライン")
                        .append(item.baselineCount()).append("件）");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
