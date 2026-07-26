package com.buzzanalysis.domain.preprocessing;

import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.post.PostType;

/**
 * プラットフォーム固有の {@code PostType} を、SNS横断で比較可能な {@link ContentFormat} に再分類する。
 * 例えばInstagramの「リール」とTikTokの「動画」は、尺が180秒以下であればいずれも{@code SHORT_VIDEO}に
 * 分類され、Phase7（ランキング）・Phase8（共通点分析）でプラットフォームをまたいだ比較が可能になる。
 */
public class ContentFormatClassifier {

    private static final int SHORT_VIDEO_MAX_SECONDS = 180;

    public ContentFormat classify(NormalizedPost post) {
        if (post.hasVideo()) {
            Integer duration = post.videoDurationSeconds();
            boolean isShort = duration != null && duration <= SHORT_VIDEO_MAX_SECONDS;
            return isShort ? ContentFormat.SHORT_VIDEO : ContentFormat.LONG_VIDEO;
        }
        if (post.mediaCount() > 1) {
            return ContentFormat.MULTI_IMAGE_CAROUSEL;
        }
        if (post.postType() == PostType.TEXT) {
            return ContentFormat.TEXT_ONLY;
        }
        return ContentFormat.SINGLE_IMAGE;
    }
}
