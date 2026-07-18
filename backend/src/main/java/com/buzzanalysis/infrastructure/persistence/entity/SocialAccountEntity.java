package com.buzzanalysis.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.UUID;

/** {@code social_accounts} テーブルに対応するJPAエンティティ。 */
@Entity
@Table(name = "social_accounts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"platform", "external_account_id"})
})
public class SocialAccountEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlatformEnum platform;

    @Column(name = "external_account_id", nullable = false)
    private String externalAccountId;

    @Column(nullable = false)
    private String username;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "follower_count")
    private Long followerCount;

    @Column(name = "post_count")
    private Long postCount;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected SocialAccountEntity() {
    }

    public SocialAccountEntity(UUID id, PlatformEnum platform, String externalAccountId, String username,
                                String displayName, String profileUrl, Long followerCount, Long postCount,
                                OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.platform = platform;
        this.externalAccountId = externalAccountId;
        this.username = username;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.followerCount = followerCount;
        this.postCount = postCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void update(String username, String displayName, String profileUrl, Long followerCount, Long postCount,
                        OffsetDateTime updatedAt) {
        this.username = username;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.followerCount = followerCount;
        this.postCount = postCount;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public PlatformEnum getPlatform() {
        return platform;
    }

    public String getExternalAccountId() {
        return externalAccountId;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public Long getFollowerCount() {
        return followerCount;
    }

    public Long getPostCount() {
        return postCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
