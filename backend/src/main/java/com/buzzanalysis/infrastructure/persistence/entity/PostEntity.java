package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** {@code posts} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "posts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"platform", "external_id"})
})
public class PostEntity {

    @Id
    private UUID id;

    @Column(name = "social_account_id", nullable = false)
    private UUID socialAccountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlatformEnum platform;

    @Column(name = "external_id")
    private String externalId;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "author_name")
    private String authorName;

    @Lob
    @Column(name = "caption")
    private String caption;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_hashtags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "hashtag")
    private List<String> hashtags = new ArrayList<>();

    @Column(name = "like_count")
    private Long likeCount;

    @Column(name = "comment_count")
    private Long commentCount;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "share_count")
    private Long shareCount;

    @Column(name = "video_duration_seconds")
    private Integer videoDurationSeconds;

    @Column(name = "image_count")
    private Integer imageCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", length = 20)
    private PostTypeEnum postType;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected PostEntity() {
    }

    public PostEntity(UUID id, UUID socialAccountId, PlatformEnum platform, String externalId, String url,
                       OffsetDateTime publishedAt, String authorName, String caption, List<String> hashtags,
                       Long likeCount, Long commentCount, Long viewCount, Long shareCount,
                       Integer videoDurationSeconds, Integer imageCount, PostTypeEnum postType,
                       OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.socialAccountId = socialAccountId;
        this.platform = platform;
        this.externalId = externalId;
        this.url = url;
        this.publishedAt = publishedAt;
        this.authorName = authorName;
        this.caption = caption;
        this.hashtags = hashtags == null ? new ArrayList<>() : new ArrayList<>(hashtags);
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

    public void updateMetrics(String caption, List<String> hashtags, Long likeCount, Long commentCount,
                               Long viewCount, Long shareCount, Integer videoDurationSeconds, Integer imageCount,
                               PostTypeEnum postType, OffsetDateTime updatedAt) {
        this.caption = caption;
        this.hashtags = hashtags == null ? new ArrayList<>() : new ArrayList<>(hashtags);
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.shareCount = shareCount;
        this.videoDurationSeconds = videoDurationSeconds;
        this.imageCount = imageCount;
        this.postType = postType;
        this.updatedAt = updatedAt;
    }

    public enum PostTypeEnum {
        REEL, IMAGE, VIDEO, CAROUSEL, TEXT
    }

    public UUID getId() {
        return id;
    }

    public UUID getSocialAccountId() {
        return socialAccountId;
    }

    public PlatformEnum getPlatform() {
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

    public PostTypeEnum getPostType() {
        return postType;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
