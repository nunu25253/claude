package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.competitor.AiCompetitorDifferencePort;
import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * {@link AiCompetitorDifferencePort} のOpenAI実装。2アカウント分の競合統計を比較し、差分を自然言語で
 * 説明する。APIキー未設定時、またはAPI呼び出しに失敗した場合は、主要指標の数値差分を機械的に
 * 文章化したルールベースのフォールバックを返す。
 */
@Service
public class OpenAiCompetitorDifferenceService implements AiCompetitorDifferencePort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiCompetitorDifferenceService.class);

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;

    public OpenAiCompetitorDifferenceService(OpenAiClient openAiClient, OpenAiProperties properties) {
        this.openAiClient = openAiClient;
        this.properties = properties;
    }

    private static final String SYSTEM_PROMPT = """
            あなたはSNSマーケティングのコンサルタントです。2つのアカウントの統計情報が与えられます。
            自社(target)と競合(competitor)の違いを分析し、自社が競合に対して優れている点・劣っている点・
            改善のヒントを日本語の文章で簡潔に説明してください（JSON形式ではなく、説明文のみを出力してください）。
            """;

    @Override
    public String explainDifference(CompetitorStatsDto target, CompetitorStatsDto competitor) {
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using rule-based fallback competitor difference explanation");
            return fallbackExplanation(target, competitor);
        }
        try {
            String userPrompt = buildPrompt(target, competitor);
            return openAiClient.chatCompleteAsPlainText(SYSTEM_PROMPT, userPrompt);
        } catch (Exception e) {
            log.warn("OpenAI competitor difference analysis failed, falling back to rule-based analysis: {}", e.getMessage());
            return fallbackExplanation(target, competitor);
        }
    }

    private String buildPrompt(CompetitorStatsDto target, CompetitorStatsDto competitor) {
        return """
                【自社(target)】平均いいね=%.1f, 平均コメント=%.1f, 平均再生数=%s, 投稿頻度(週)=%.1f, 平均文字数=%.1f, 平均動画時間=%s秒
                【競合(competitor)】平均いいね=%.1f, 平均コメント=%.1f, 平均再生数=%s, 投稿頻度(週)=%.1f, 平均文字数=%.1f, 平均動画時間=%s秒
                """.formatted(
                target.averageLikeCount(), target.averageCommentCount(), nullToDash(target.averageViewCount()),
                target.postingFrequencyPerWeek(), target.averageCaptionLength(), nullToDash(target.averageVideoDurationSeconds()),
                competitor.averageLikeCount(), competitor.averageCommentCount(), nullToDash(competitor.averageViewCount()),
                competitor.postingFrequencyPerWeek(), competitor.averageCaptionLength(), nullToDash(competitor.averageVideoDurationSeconds())
        );
    }

    private String nullToDash(Double value) {
        return value == null ? "未計測" : String.valueOf(value);
    }

    private String fallbackExplanation(CompetitorStatsDto target, CompetitorStatsDto competitor) {
        StringBuilder sb = new StringBuilder("（ルールベース簡易分析: OpenAI未接続のため主要指標の数値差分のみ表示）\n");
        appendComparison(sb, "平均いいね数", target.averageLikeCount(), competitor.averageLikeCount());
        appendComparison(sb, "平均コメント数", target.averageCommentCount(), competitor.averageCommentCount());
        appendComparison(sb, "投稿頻度(週あたり)", target.postingFrequencyPerWeek(), competitor.postingFrequencyPerWeek());
        if (target.averageViewCount() != null && competitor.averageViewCount() != null) {
            appendComparison(sb, "平均再生数", target.averageViewCount(), competitor.averageViewCount());
        }
        return sb.toString();
    }

    private void appendComparison(StringBuilder sb, String label, double targetValue, double competitorValue) {
        if (targetValue > competitorValue) {
            sb.append(String.format("・%s は競合より高い（自社%.1f / 競合%.1f）%n", label, targetValue, competitorValue));
        } else if (targetValue < competitorValue) {
            sb.append(String.format("・%s は競合より低い（自社%.1f / 競合%.1f）%n", label, targetValue, competitorValue));
        } else {
            sb.append(String.format("・%s は競合と同水準（%.1f）%n", label, targetValue));
        }
    }
}
