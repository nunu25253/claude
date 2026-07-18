package com.buzzanalysis.domain.normalization.strategy;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;

/**
 * X (旧Twitter) 向け正規化ルール。
 * {@code docs/phases/00_sns_data_constraints.md} の前提: 第三者投稿の再生数(インプレッション数)は
 * 原則として公開APIでは取得できない（投稿者本人のみ閲覧可能な指標）ため、常に未計測として扱う。
 * リツイート数はシェア相当の指標として公開APIで取得可能。
 */
public class XNormalizationStrategy implements PlatformNormalizationStrategy {

    @Override
    public boolean supports(Platform platform) {
        return platform == Platform.X;
    }

    @Override
    public int mediaCount(Post post) {
        return post.getImageCount() != null && post.getImageCount() > 0 ? post.getImageCount() : 1;
    }

    @Override
    public boolean hasVideo(Post post) {
        return post.getVideoDurationSeconds() != null;
    }

    @Override
    public boolean isViewCountMeasured(Post post) {
        // 第三者の投稿インプレッション数は公開APIでは取得不可（自分自身の投稿のみ閲覧可能）。
        return false;
    }

    @Override
    public boolean isShareCountMeasured(Post post) {
        return true;
    }
}
