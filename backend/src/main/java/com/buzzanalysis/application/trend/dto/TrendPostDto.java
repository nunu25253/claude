package com.buzzanalysis.application.trend.dto;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * {@code GET /api/v1/trends} が返す投稿1件分。フロントエンドの{@code Post}型に対応する。
 * バックエンドは投稿者の表示名とハンドル名を区別して保持しないため、両フィールドとも
 * {@link Post#getAuthorName()} を充てる。サムネイルURL・アバターURL・保存数は
 * バックエンドで保持していないため常にnull。
 */
public record TrendPostDto(
        UUID id,
        String url,
        Platform platform,
        String genre,
        UUID accountId,
        String accountName,
        String accountHandle,
        String accountAvatarUrl,
        String thumbnailUrl,
        String caption,
        List<String> hashtags,
        OffsetDateTime postedAt,
        Long likeCount,
        Long commentCount,
        Long shareCount,
        Long saveCount,
        Long viewCount,
        Integer videoDurationSeconds,
        Integer carouselCount,
        Integer characterCount,
        Double buzzScore
) {
    public static TrendPostDto from(Post post, String genre, Double buzzScore) {
        return new TrendPostDto(
                post.getId(),
                post.getUrl(),
                post.getPlatform(),
                genre,
                post.getSocialAccountId(),
                post.getAuthorName(),
                post.getAuthorName(),
                null,
                null,
                post.getCaption(),
                post.getHashtags(),
                post.getPublishedAt(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getShareCount(),
                null,
                post.getViewCount(),
                post.getVideoDurationSeconds(),
                post.getImageCount(),
                post.getCaption() == null ? null : post.getCaption().length(),
                buzzScore
        );
    }
}
