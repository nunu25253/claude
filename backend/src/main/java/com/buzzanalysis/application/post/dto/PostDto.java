package com.buzzanalysis.application.post.dto;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Post集約のapplication層向けDTO。 */
public record PostDto(
        UUID id,
        Platform platform,
        String externalId,
        String url,
        OffsetDateTime publishedAt,
        String authorName,
        String caption,
        List<String> hashtags,
        Long likeCount,
        Long commentCount,
        Long viewCount,
        Long shareCount,
        Integer videoDurationSeconds,
        Integer imageCount,
        PostType postType
) {
    public static PostDto from(Post post) {
        return new PostDto(
                post.getId(), post.getPlatform(), post.getExternalId(), post.getUrl(), post.getPublishedAt(),
                post.getAuthorName(), post.getCaption(), post.getHashtags(), post.getLikeCount(),
                post.getCommentCount(), post.getViewCount(), post.getShareCount(),
                post.getVideoDurationSeconds(), post.getImageCount(), post.getPostType()
        );
    }
}
