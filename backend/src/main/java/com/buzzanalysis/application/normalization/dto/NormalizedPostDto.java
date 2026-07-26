package com.buzzanalysis.application.normalization.dto;

import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.PostType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** {@link NormalizedPost}（ドメイン）のapplication層向けDTO。Phase2以降・REST APIから参照される。 */
public record NormalizedPostDto(
        UUID postId,
        UUID accountId,
        Platform platform,
        PostType postType,
        String url,
        OffsetDateTime publishedAt,
        String authorName,
        String rawText,
        List<String> hashtags,
        Long likeCount,
        Long commentCount,
        Long viewCount,
        Long shareCount,
        Integer videoDurationSeconds,
        int mediaCount,
        boolean hasVideo,
        Double engagementRate,
        long postAgeInHours,
        Set<String> unmeasuredMetrics
) {
    public static NormalizedPostDto from(NormalizedPost normalizedPost) {
        return new NormalizedPostDto(
                normalizedPost.postId(), normalizedPost.accountId(), normalizedPost.platform(),
                normalizedPost.postType(), normalizedPost.url(), normalizedPost.publishedAt(),
                normalizedPost.authorName(), normalizedPost.rawText(), normalizedPost.hashtags(),
                normalizedPost.likeCount(), normalizedPost.commentCount(), normalizedPost.viewCount(),
                normalizedPost.shareCount(), normalizedPost.videoDurationSeconds(), normalizedPost.mediaCount(),
                normalizedPost.hasVideo(), normalizedPost.engagementRate(), normalizedPost.postAgeInHours(),
                normalizedPost.unmeasuredMetrics()
        );
    }
}
