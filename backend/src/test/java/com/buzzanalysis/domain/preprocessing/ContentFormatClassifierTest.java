package com.buzzanalysis.domain.preprocessing;

import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.PostType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContentFormatClassifierTest {

    private final ContentFormatClassifier classifier = new ContentFormatClassifier();

    @Test
    void classify_returnsShortVideo_forVideoUnder180Seconds() {
        NormalizedPost post = normalizedPost(PostType.REEL, true, 45, 1);
        assertThat(classifier.classify(post)).isEqualTo(ContentFormat.SHORT_VIDEO);
    }

    @Test
    void classify_returnsLongVideo_forVideoOver180Seconds() {
        NormalizedPost post = normalizedPost(PostType.VIDEO, true, 400, 1);
        assertThat(classifier.classify(post)).isEqualTo(ContentFormat.LONG_VIDEO);
    }

    @Test
    void classify_returnsMultiImageCarousel_forCarouselWithMultipleMedia() {
        NormalizedPost post = normalizedPost(PostType.CAROUSEL, false, null, 6);
        assertThat(classifier.classify(post)).isEqualTo(ContentFormat.MULTI_IMAGE_CAROUSEL);
    }

    @Test
    void classify_returnsSingleImage_forSingleImagePost() {
        NormalizedPost post = normalizedPost(PostType.IMAGE, false, null, 1);
        assertThat(classifier.classify(post)).isEqualTo(ContentFormat.SINGLE_IMAGE);
    }

    @Test
    void classify_returnsTextOnly_forTextPostWithNoMedia() {
        NormalizedPost post = normalizedPost(PostType.TEXT, false, null, 1);
        assertThat(classifier.classify(post)).isEqualTo(ContentFormat.TEXT_ONLY);
    }

    private NormalizedPost normalizedPost(PostType postType, boolean hasVideo, Integer videoDurationSeconds, int mediaCount) {
        return NormalizedPost.builder()
                .postId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .platform(Platform.INSTAGRAM)
                .postType(postType)
                .url("https://example.com/post")
                .hasVideo(hasVideo)
                .videoDurationSeconds(videoDurationSeconds)
                .mediaCount(mediaCount)
                .build();
    }
}
