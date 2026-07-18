package com.buzzanalysis.domain.post;

import com.buzzanalysis.domain.platform.FetchedPostData;
import com.buzzanalysis.domain.platform.Platform;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * SNS投稿を表す集約ルート。公開されている指標のみを保持する
 * （インプレッション・リーチ・保存数などの非公開データは対象外）。
 */
public class Post {

    private final UUID id;
    private final UUID socialAccountId;
    private final Platform platform;
    private final String externalId;
    private final String url;
    private final OffsetDateTime publishedAt;
    private final String authorName;
    private String caption;
    private List<String> hashtags;
    private Long likeCount;
    private Long commentCount;
    private Long viewCount;
    private Long shareCount;
    private Integer videoDurationSeconds;
    private Integer imageCount;
    private PostType postType;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Post(UUID id, UUID socialAccountId, Platform platform, String externalId, String url,
                OffsetDateTime publishedAt, String authorName, String caption, List<String> hashtags,
                Long likeCount, Long commentCount, Long viewCount, Long shareCount,
                Integer videoDurationSeconds, Integer imageCount, PostType postType,
                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.socialAccountId = socialAccountId;
        this.platform = Objects.requireNonNull(platform);
        this.externalId = externalId;
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.publishedAt = publishedAt;
        this.authorName = authorName;
        this.caption = caption;
        this.hashtags = hashtags == null ? List.of() : hashtags;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.shareCount = shareCount;
        this.videoDurationSeconds = videoDurationSeconds;
        this.imageCount = imageCount;
        this.postType = postType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /** 外部APIから取得した投稿データとアカウントIDから新しいPostを生成するファクトリメソッド。 */
    public static Post fromFetchedData(UUID socialAccountId, Platform platform, FetchedPostData data) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Post(
                UUID.randomUUID(),
                socialAccountId,
                platform,
                data.externalId(),
                data.url(),
                data.publishedAt(),
                data.authorName(),
                data.caption(),
                data.hashtags(),
                data.likeCount(),
                data.commentCount(),
                data.viewCount(),
                data.shareCount(),
                data.videoDurationSeconds(),
                data.imageCount(),
                data.postType(),
                now,
                now
        );
    }

    /** 最新の公開指標で投稿データを更新する（再取得時に使用）。 */
    public void refreshMetrics(FetchedPostData data) {
        this.caption = data.caption();
        this.hashtags = data.hashtags() == null ? List.of() : data.hashtags();
        this.likeCount = data.likeCount();
        this.commentCount = data.commentCount();
        this.viewCount = data.viewCount();
        this.shareCount = data.shareCount();
        this.videoDurationSeconds = data.videoDurationSeconds();
        this.imageCount = data.imageCount();
        this.postType = data.postType();
        this.updatedAt = OffsetDateTime.now();
    }

    /** エンゲージメント率 (いいね+コメント) / max(閲覧数,1) を計算する。閲覧数がない場合はnull。 */
    public Double engagementRate() {
        if (viewCount == null || viewCount == 0) {
            return null;
        }
        long engagements = (likeCount == null ? 0 : likeCount) + (commentCount == null ? 0 : commentCount);
        return engagements / (double) viewCount;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSocialAccountId() {
        return socialAccountId;
    }

    public Platform getPlatform() {
        return platform;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getUrl() {
        return url;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getCaption() {
        return caption;
    }

    public List<String> getHashtags() {
        return hashtags;
    }

    public Long getLikeCount() {
        return likeCount;
    }

    public Long getCommentCount() {
        return commentCount;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public Long getShareCount() {
        return shareCount;
    }

    public Integer getVideoDurationSeconds() {
        return videoDurationSeconds;
    }

    public Integer getImageCount() {
        return imageCount;
    }

    public PostType getPostType() {
        return postType;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
