package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.post.AiPostAnalysisPort;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * {@link AiPostAnalysisPort} のOpenAI実装。投稿データをプロンプトに埋め込みJSON形式で分析結果を要求する。
 * APIキー未設定時、またはAPI呼び出しに失敗した場合は、投稿の公開指標に基づくルールベースの
 * フォールバック分析を返す（本番同様のレスポンス構造を維持するため）。
 */
@Service
public class OpenAiAnalysisService implements AiPostAnalysisPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiAnalysisService.class);

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiAnalysisService(OpenAiClient openAiClient, OpenAiProperties properties, ObjectMapper objectMapper) {
        this.openAiClient = openAiClient;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiAnalysisOutput analyze(Post post) {
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using rule-based fallback analysis for post={}", post.getId());
            return fallbackAnalysis(post);
        }
        try {
            String userPrompt = buildPrompt(post);
            String responseJson = openAiClient.chatComplete(SYSTEM_PROMPT, userPrompt);
            return parseResponse(responseJson);
        } catch (Exception e) {
            log.warn("OpenAI analysis failed, falling back to rule-based analysis for post={}: {}", post.getId(), e.getMessage());
            return fallbackAnalysis(post);
        }
    }

    private static final String SYSTEM_PROMPT = """
            あなたはSNSのバズ分析専門家です。与えられた投稿の公開データから、なぜバズった/バズらなかったかを分析し、
            以下のキーを持つJSONオブジェクトのみを出力してください（説明文やコードブロック記法は不要）：
            genre, subGenre, whyItWentViral, targetAudience, postPurpose, hook, callToAction,
            postStructureAnalysis, sentimentAnalysis, sentimentScore(0.0-1.0),
            videoStructureAnalysis, carouselStructureAnalysis, titleAnalysis, textAnalysis, postingTimeAnalysis,
            hashtagAnalysis, strengths, weaknesses, improvementSuggestions, viralPotentialHint(0.0-1.0)

            各キーの意味:
            genre=投稿の大分類ジャンル(例: 美容, グルメ, ガジェット), subGenre=ジャンルの下位分類,
            postPurpose=投稿の目的(例: 認知獲得, 商品訴求, フォロワー獲得, エンゲージメント獲得),
            postStructureAnalysis=投稿種別によらない全体の構成分析(フック→本編→CTAの流れ等),
            strengths=この投稿の強み, weaknesses=この投稿の弱み・改善余地
            """;

    private String buildPrompt(Post post) {
        return """
                プラットフォーム: %s
                投稿種別: %s
                キャプション: %s
                ハッシュタグ: %s
                いいね数: %s
                コメント数: %s
                再生数: %s
                投稿日時: %s
                動画時間(秒): %s
                画像枚数: %s
                """.formatted(
                post.getPlatform(), post.getPostType(), nullToDash(post.getCaption()),
                String.join(", ", post.getHashtags()), post.getLikeCount(), post.getCommentCount(),
                post.getViewCount(), post.getPublishedAt(), post.getVideoDurationSeconds(), post.getImageCount()
        );
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private AiAnalysisOutput parseResponse(String responseJson) throws Exception {
        JsonNode node = objectMapper.readTree(responseJson);
        return new AiAnalysisOutput(
                textOrDefault(node, "genre"),
                textOrDefault(node, "subGenre"),
                textOrDefault(node, "whyItWentViral"),
                textOrDefault(node, "targetAudience"),
                textOrDefault(node, "postPurpose"),
                textOrDefault(node, "hook"),
                textOrDefault(node, "callToAction"),
                textOrDefault(node, "postStructureAnalysis"),
                textOrDefault(node, "sentimentAnalysis"),
                doubleOrDefault(node, "sentimentScore", 0.5),
                textOrDefault(node, "videoStructureAnalysis"),
                textOrDefault(node, "carouselStructureAnalysis"),
                textOrDefault(node, "titleAnalysis"),
                textOrDefault(node, "textAnalysis"),
                textOrDefault(node, "postingTimeAnalysis"),
                textOrDefault(node, "hashtagAnalysis"),
                textOrDefault(node, "strengths"),
                textOrDefault(node, "weaknesses"),
                textOrDefault(node, "improvementSuggestions"),
                doubleOrDefault(node, "viralPotentialHint", 0.5)
        );
    }

    private String textOrDefault(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText();
    }

    private double doubleOrDefault(JsonNode node, String field, double defaultValue) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? defaultValue : value.asDouble(defaultValue);
    }

    /**
     * OpenAI未接続時のルールベース分析。投稿の公開指標から簡易的な説明文を組み立てる。
     */
    private AiAnalysisOutput fallbackAnalysis(Post post) {
        long likes = post.getLikeCount() == null ? 0 : post.getLikeCount();
        long comments = post.getCommentCount() == null ? 0 : post.getCommentCount();
        Long views = post.getViewCount();
        PostType type = post.getPostType();
        int hashtagCount = post.getHashtags() == null ? 0 : post.getHashtags().size();

        double engagementRatio = (views != null && views > 0) ? (likes + comments) / (double) views : 0.05;
        double sentimentScore = Math.min(1.0, 0.5 + engagementRatio * 2);
        double viralPotential = Math.min(1.0, 0.3 + engagementRatio * 3);

        String format = type == null ? "投稿" : type.name().toLowerCase(Locale.ROOT);
        List<String> hashtags = post.getHashtags();
        String genre = (hashtags != null && !hashtags.isEmpty())
                ? hashtags.get(0) + "（ハッシュタグから簡易推定。本分析はOpenAI未接続時の簡易フォールバックです）"
                : "未分類（ルールベース簡易分析ではジャンルを判定できません）";
        String subGenre = (hashtags != null && hashtags.size() > 1) ? hashtags.get(1) : "-";

        return new AiAnalysisOutput(
                genre,
                subGenre,
                "エンゲージメント率が%.2f%%と高く、%sフォーマットが視聴者の関心を引いたと推測されます（ルールベース簡易分析）。"
                        .formatted(engagementRatio * 100, format),
                "投稿内容とハッシュタグ傾向から、トレンドに敏感な10〜30代の視聴者層が中心と推測されます。",
                engagementRatio > 0.1 ? "エンゲージメント獲得（推測）" : "認知獲得（推測、ルールベース簡易分析）",
                post.getCaption() != null && !post.getCaption().isBlank()
                        ? "冒頭: 「" + truncate(post.getCaption(), 30) + "」が興味を引くフックになっています。"
                        : "キャプションが未取得のため、動画冒頭の視覚的インパクトがフックと推測されます。",
                "プロフィールへの誘導やコメントでの問いかけがCTAとして機能している可能性があります。",
                "フック→本編→CTAの基本構成に沿っていると推測されます（ルールベース簡易分析のため詳細な構成は判定できません）。",
                sentimentScore >= 0.6 ? "全体的にポジティブな反応が多いと推測されます。" : "反応はやや中立〜控えめと推測されます。",
                sentimentScore,
                type == PostType.VIDEO || type == PostType.REEL
                        ? "動画の長さ(%s秒)がテンポの良い視聴体験を作っていると推測されます。".formatted(post.getVideoDurationSeconds())
                        : "動画コンテンツではないため対象外です。",
                type == PostType.CAROUSEL
                        ? "カルーセル(%s枚)がスワイプを誘発する構成になっていると推測されます。".formatted(post.getImageCount())
                        : "カルーセル投稿ではないため対象外です。",
                "簡潔で興味を引くタイトル/冒頭文になっていると推測されます。",
                "文章量・改行の使い方は読みやすさに配慮されていると推測されます。",
                "投稿時間帯はアクティブユーザーが多い時間帯と重なっている可能性があります。",
                hashtagCount + "個のハッシュタグが使用されており、発見性に一定の効果があると推測されます。",
                engagementRatio > 0.1 ? "エンゲージメント率の高さが強みと推測されます。" : "公開指標からは明確な強みを特定できません（ルールベース簡易分析）。",
                "CTAの明確化・投稿頻度の最適化に改善余地があると推測されます（ルールベース簡易分析）。",
                "類似投稿のパフォーマンスを参考に、CTAの明確化と投稿頻度の最適化を検討してください（本分析はOpenAI未接続時の簡易フォールバックです）。",
                viralPotential
        );
    }

    private String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength) + "...";
    }
}
