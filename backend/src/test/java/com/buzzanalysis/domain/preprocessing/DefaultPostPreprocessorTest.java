package com.buzzanalysis.domain.preprocessing;

import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.steps.EmojiRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.HtmlRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.UrlRemovalStep;
import com.buzzanalysis.domain.preprocessing.steps.WhitespaceNormalizationStep;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultPostPreprocessor}（Phase2オーケストレーター）の単体テスト。
 * すべて実オブジェクト（フレームワーク非依存のPOJO）で構成し、Mockitoは使わない。
 */
class DefaultPostPreprocessorTest {

    private final DefaultPostPreprocessor preprocessor = new DefaultPostPreprocessor(
            new DefaultTextCleaner(List.of(
                    new HtmlRemovalStep(), new UrlRemovalStep(), new EmojiRemovalStep(), new WhitespaceNormalizationStep())),
            new HeuristicLanguageDetector(),
            new HashtagExtractor(),
            new MentionExtractor(),
            new PostingTimeAnalyzer(),
            new VideoDurationAnalyzer(),
            new ContentFormatClassifier()
    );

    @Test
    void preprocess_combinesAllAnalysisResults() {
        UUID postId = UUID.randomUUID();
        NormalizedPost normalizedPost = NormalizedPost.builder()
                .postId(postId)
                .accountId(UUID.randomUUID())
                .platform(Platform.TIKTOK)
                .postType(PostType.VIDEO)
                .url("https://www.tiktok.com/@creator/video/1")
                .publishedAt(OffsetDateTime.of(2026, 7, 17, 19, 0, 0, 0, ZoneOffset.UTC))
                .authorName("creator")
                .rawText("新商品発売🎉 詳細は https://example.com をチェック #新商品 @brand_official")
                .hashtags(List.of("新商品"))
                .hasVideo(true)
                .videoDurationSeconds(30)
                .mediaCount(1)
                .build();

        PreprocessedPost result = preprocessor.preprocess(normalizedPost);

        assertThat(result.postId()).isEqualTo(postId);
        assertThat(result.cleanText()).doesNotContain("🎉", "https://").contains("新商品発売", "をチェック");
        assertThat(result.language()).isEqualTo(Language.JAPANESE);
        assertThat(result.hashtags()).containsExactly("新商品");
        assertThat(result.mentions()).containsExactly("brand_official");
        assertThat(result.postingTime().timeSlot()).isEqualTo(TimeSlot.EVENING);
        assertThat(result.videoDuration().category()).isEqualTo(DurationCategory.MEDIUM);
        assertThat(result.contentFormat()).isEqualTo(ContentFormat.SHORT_VIDEO);
    }
}
