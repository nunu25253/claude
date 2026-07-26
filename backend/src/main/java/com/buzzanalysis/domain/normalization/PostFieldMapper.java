package com.buzzanalysis.domain.normalization;

import com.buzzanalysis.domain.post.Post;

import java.time.Duration;
import java.time.OffsetDateTime;

/**
 * {@link Post}（既存の投稿集約）から {@link NormalizedPost} へ、プラットフォームによらず共通の
 * フィールドをそのまま写し取る、純粋な構造マッピング（Mapperパターン）。
 * プラットフォーム固有の差異（メディア件数の数え方や、指標が公開情報として信頼できるか等）は
 * このクラスの責務ではなく、{@link com.buzzanalysis.domain.normalization.strategy.PlatformNormalizationStrategy}
 * に委ねる。フレームワークに依存しない純粋なドメインサービスであり、Springには依存しない。
 */
public class PostFieldMapper {

    /** {@code Post} の共通フィールドを {@link NormalizedPost.Builder} に写し取る。 */
    public NormalizedPost.Builder toBuilder(Post post) {
        return NormalizedPost.builder()
                .postId(post.getId())
                .accountId(post.getSocialAccountId())
                .platform(post.getPlatform())
                .postType(post.getPostType())
                .url(post.getUrl())
                .publishedAt(post.getPublishedAt())
                .authorName(post.getAuthorName())
                .rawText(post.getCaption())
                .hashtags(post.getHashtags())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .videoDurationSeconds(post.getVideoDurationSeconds())
                .postAgeInHours(postAgeInHours(post.getPublishedAt()));
    }

    private long postAgeInHours(OffsetDateTime publishedAt) {
        if (publishedAt == null) {
            return 0L;
        }
        return Math.max(0L, Duration.between(publishedAt, OffsetDateTime.now()).toHours());
    }
}
