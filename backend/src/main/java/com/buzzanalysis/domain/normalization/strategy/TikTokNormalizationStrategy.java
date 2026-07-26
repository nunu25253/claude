package com.buzzanalysis.domain.normalization.strategy;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;

/**
 * TikTok向け正規化ルール。TikTokの投稿は常に単一の動画（カルーセルは存在しない）であり、
 * Display APIは再生数・シェア数のいずれも公開情報として提供する（{@code 00_sns_data_constraints.md} 参照）。
 */
public class TikTokNormalizationStrategy implements PlatformNormalizationStrategy {

    @Override
    public boolean supports(Platform platform) {
        return platform == Platform.TIKTOK;
    }

    @Override
    public int mediaCount(Post post) {
        return 1;
    }

    @Override
    public boolean hasVideo(Post post) {
        return true;
    }

    @Override
    public boolean isViewCountMeasured(Post post) {
        return true;
    }

    @Override
    public boolean isShareCountMeasured(Post post) {
        return true;
    }
}
