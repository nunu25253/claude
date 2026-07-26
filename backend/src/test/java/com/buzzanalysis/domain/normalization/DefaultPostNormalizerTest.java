package com.buzzanalysis.domain.normalization;

import com.buzzanalysis.domain.normalization.strategy.DefaultNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.InstagramNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.PlatformNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.TikTokNormalizationStrategy;
import com.buzzanalysis.domain.normalization.strategy.XNormalizationStrategy;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultPostNormalizer}（Phase1: 正規化レイヤー）の単体テスト。
 * ドメイン層は純粋なPOJOのため、Mockitoを使わずすべて実オブジェクトで検証する。
 * 「未計測の指標はnullのまま保持し、0や推定値で埋めない」という設計方針の検証を重視する。
 */
class DefaultPostNormalizerTest {

    private DefaultPostNormalizer normalizer;

    @BeforeEach
    void setUp() {
        List<PlatformNormalizationStrategy> strategies = List.of(
                new InstagramNormalizationStrategy(),
                new TikTokNormalizationStrategy(),
                new XNormalizationStrategy(),
                new DefaultNormalizationStrategy()
        );
        normalizer = new DefaultPostNormalizer(new PostFieldMapper(), strategies);
    }

    @Test
    void instagramReel_hasMeasuredViewCount_andComputedEngagementRate() {
        Post post = post(Platform.INSTAGRAM, PostType.REEL, 1000L, 100L, 10_000L, null, null, null);

        NormalizedPost result = normalizer.normalize(post);

        assertThat(result.viewCount()).isEqualTo(10_000L);
        assertThat(result.hasVideo()).isTrue();
        assertThat(result.mediaCount()).isEqualTo(1);
        assertThat(result.engagementRate()).isEqualTo((1000.0 + 100.0) / 10_000.0);
        assertThat(result.unmeasuredMetrics()).contains("shareCount").doesNotContain("viewCount");
    }

    @Test
    void instagramCarousel_hasNoMeasuredViewCount_evenIfRawValuePresent() {
        // Instagramのカルーセル投稿には公開の再生数が存在しないため、生データに値が入っていてもnullとして扱う。
        Post post = post(Platform.INSTAGRAM, PostType.CAROUSEL, 500L, 20L, 9_999L, null, 5, null);

        NormalizedPost result = normalizer.normalize(post);

        assertThat(result.viewCount()).isNull();
        assertThat(result.engagementRate()).isNull();
        assertThat(result.mediaCount()).isEqualTo(5);
        assertThat(result.hasVideo()).isFalse();
        assertThat(result.unmeasuredMetrics()).contains("viewCount", "shareCount");
    }

    @Test
    void tikTokVideo_hasBothViewCountAndShareCountMeasured() {
        Post post = post(Platform.TIKTOK, PostType.VIDEO, 2000L, 300L, 50_000L, 400L, null, 30);

        NormalizedPost result = normalizer.normalize(post);

        assertThat(result.viewCount()).isEqualTo(50_000L);
        assertThat(result.shareCount()).isEqualTo(400L);
        assertThat(result.unmeasuredMetrics()).isEmpty();
    }

    @Test
    void xPost_neverHasMeasuredViewCount_butShareCountIsMeasured() {
        // Xの第三者投稿インプレッション数(view数)は公開APIでは取得不可。
        Post post = post(Platform.X, PostType.TEXT, 800L, 50L, 123_456L, 60L, null, null);

        NormalizedPost result = normalizer.normalize(post);

        assertThat(result.viewCount()).isNull();
        assertThat(result.shareCount()).isEqualTo(60L);
        assertThat(result.unmeasuredMetrics()).containsExactly("viewCount");
    }

    @Test
    void unsupportedPlatform_fallsBackToDefaultStrategy_treatingAllMetricsAsUnmeasured() {
        Post post = post(Platform.YOUTUBE, PostType.VIDEO, 100L, 10L, 999L, 20L, null, 120);

        NormalizedPost result = normalizer.normalize(post);

        assertThat(result.viewCount()).isNull();
        assertThat(result.shareCount()).isNull();
        assertThat(result.hasVideo()).isTrue();
        assertThat(result.unmeasuredMetrics()).containsExactlyInAnyOrder("viewCount", "shareCount");
    }

    private Post post(Platform platform, PostType postType, Long likeCount, Long commentCount,
                       Long viewCount, Long shareCount, Integer imageCount, Integer videoDurationSeconds) {
        return new Post(UUID.randomUUID(), UUID.randomUUID(), platform, "ext-1", "https://example.com/post/1",
                OffsetDateTime.now().minusHours(3), "creator", "caption text", List.of("tag1", "tag2"),
                likeCount, commentCount, viewCount, shareCount, videoDurationSeconds, imageCount, postType,
                OffsetDateTime.now(), OffsetDateTime.now());
    }
}
