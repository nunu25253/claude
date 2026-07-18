package com.buzzanalysis.domain.normalization.strategy;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;

/**
 * Instagram向け正規化ルール。
 * {@code docs/phases/00_sns_data_constraints.md} の前提: 再生数はReels/動画投稿でのみ公開情報として
 * 存在し、画像・カルーセル投稿には存在しない。シェア数はGraph APIでは一般的に公開されない。
 */
public class InstagramNormalizationStrategy implements PlatformNormalizationStrategy {

    @Override
    public boolean supports(Platform platform) {
        return platform == Platform.INSTAGRAM;
    }

    @Override
    public int mediaCount(Post post) {
        if (post.getPostType() == PostType.CAROUSEL && post.getImageCount() != null && post.getImageCount() > 0) {
            return post.getImageCount();
        }
        return 1;
    }

    @Override
    public boolean hasVideo(Post post) {
        return post.getPostType() == PostType.REEL || post.getPostType() == PostType.VIDEO;
    }

    @Override
    public boolean isViewCountMeasured(Post post) {
        return post.getPostType() == PostType.REEL || post.getPostType() == PostType.VIDEO;
    }

    @Override
    public boolean isShareCountMeasured(Post post) {
        // Instagram Graph APIは大半のアカウント種別でシェア数を公開情報として提供しない。
        return false;
    }
}
