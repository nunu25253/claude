package com.buzzanalysis.infrastructure.report;

import com.buzzanalysis.application.report.command.ReportGenerationContext;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.score.BuzzScore;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * レポートのMarkdown本文を組み立てる共通ロジック。PDF/HTML出力もこのMarkdownをベースに生成することで、
 * 3つの {@code GenerateReportCommand} 実装間でコンテンツの一貫性を保つ。
 */
@Component
public class ReportContentBuilder {

    private final Parser markdownParser = Parser.builder().build();
    private final HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();

    public String buildMarkdown(ReportGenerationContext context) {
        Post post = context.post();
        AnalysisResult analysis = context.analysisResult();
        BuzzScore score = context.buzzScore();

        StringBuilder sb = new StringBuilder();
        sb.append("# SNSバズ分析レポート\n\n");
        sb.append("**投稿者:** ").append(nvl(post.getAuthorName())).append("  \n");
        sb.append("**プラットフォーム:** ").append(post.getPlatform()).append("  \n");
        sb.append("**投稿URL:** ").append(nvl(post.getUrl())).append("  \n");
        sb.append("**投稿日時:** ").append(post.getPublishedAt()).append("  \n\n");

        sb.append("## バズスコア: ").append(String.format("%.1f", score.getTotalScore())).append(" / 100\n\n");
        sb.append("| 評価項目 | スコア |\n|---|---|\n");
        for (Map.Entry<String, Double> entry : score.getBreakdown().entrySet()) {
            sb.append("| ").append(entry.getKey()).append(" | ")
                    .append(String.format("%.1f", entry.getValue())).append(" |\n");
        }
        sb.append("\n");

        sb.append("## 公開指標\n\n");
        sb.append("- いいね数: ").append(nvl(post.getLikeCount())).append("\n");
        sb.append("- コメント数: ").append(nvl(post.getCommentCount())).append("\n");
        sb.append("- 再生数: ").append(nvl(post.getViewCount())).append("\n");
        sb.append("- シェア数: ").append(nvl(post.getShareCount())).append("\n");
        sb.append("- ハッシュタグ: ").append(post.getHashtags().stream().collect(Collectors.joining(", "))).append("\n\n");

        appendSection(sb, "なぜバズったか", analysis.getWhyItWentViral());
        appendSection(sb, "ターゲット層", analysis.getTargetAudience());
        appendSection(sb, "フック", analysis.getHook());
        appendSection(sb, "CTA", analysis.getCallToAction());
        appendSection(sb, "感情分析", analysis.getSentimentAnalysis());
        appendSection(sb, "動画構成分析", analysis.getVideoStructureAnalysis());
        appendSection(sb, "カルーセル構成分析", analysis.getCarouselStructureAnalysis());
        appendSection(sb, "タイトル分析", analysis.getTitleAnalysis());
        appendSection(sb, "文章分析", analysis.getTextAnalysis());
        appendSection(sb, "投稿時間分析", analysis.getPostingTimeAnalysis());
        appendSection(sb, "ハッシュタグ分析", analysis.getHashtagAnalysis());
        appendSection(sb, "改善案", analysis.getImprovementSuggestions());

        return sb.toString();
    }

    public String buildHtml(ReportGenerationContext context) {
        String markdown = buildMarkdown(context);
        Node document = markdownParser.parse(markdown);
        String bodyHtml = htmlRenderer.render(document);
        return """
                <!DOCTYPE html>
                <html lang="ja">
                <head>
                <meta charset="UTF-8" />
                <title>SNSバズ分析レポート</title>
                <style>
                  body { font-family: 'Hiragino Sans', 'Yu Gothic', sans-serif; max-width: 860px; margin: 2rem auto; line-height: 1.7; color: #1a1a1a; }
                  table { border-collapse: collapse; width: 100%%; margin: 1rem 0; }
                  th, td { border: 1px solid #ccc; padding: 6px 10px; text-align: left; }
                  h1 { color: #7c3aed; } h2 { color: #4338ca; margin-top: 2rem; }
                </style>
                </head>
                <body>
                %s
                </body>
                </html>
                """.formatted(bodyHtml);
    }

    private void appendSection(StringBuilder sb, String title, String content) {
        sb.append("## ").append(title).append("\n\n");
        sb.append(nvl(content)).append("\n\n");
    }

    private String nvl(Object value) {
        return value == null ? "-" : String.valueOf(value);
    }
}
