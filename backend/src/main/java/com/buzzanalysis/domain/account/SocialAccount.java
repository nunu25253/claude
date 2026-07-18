package com.buzzanalysis.domain.account;

import com.buzzanalysis.domain.platform.Platform;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * SNS上のアカウント（分析対象のインフルエンサー/ブランドアカウント等）を表す集約。
 */
public class SocialAccount {

    private final UUID id;
    private final Platform platform;
    private final String externalAccountId;
    private String username;
    private String displayName;
    private String profileUrl;
    private Long followerCount;
    private Long postCount;
    /** 定期データ取得バッチ（自動同期）の対象にするかどうか。デフォルトはtrue。 */
    private boolean trackingEnabled;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public SocialAccount(UUID id, Platform platform, String externalAccountId, String username,
                          String displayName, String profileUrl, Long followerCount, Long postCount,
                          boolean trackingEnabled, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.platform = platform;
        this.externalAccountId = externalAccountId;
        this.username = username;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.followerCount = followerCount;
        this.postCount = postCount;
        this.trackingEnabled = trackingEnabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static SocialAccount createNew(Platform platform, String externalAccountId, String username,
                                           String displayName, String profileUrl, Long followerCount, Long postCount) {
        OffsetDateTime now = OffsetDateTime.now();
        return new SocialAccount(UUID.randomUUID(), platform, externalAccountId, username, displayName,
                profileUrl, followerCount, postCount, true, now, now);
    }

    /** 定期データ取得バッチの対象に含める。 */
    public void enableTracking() {
        this.trackingEnabled = true;
        this.updatedAt = OffsetDateTime.now();
    }

    /** 定期データ取得バッチの対象から外す（退会・非公開化したアカウント等）。 */
    public void disableTracking() {
        this.trackingEnabled = false;
        this.updatedAt = OffsetDateTime.now();
    }

    /** 最新の公開プロフィール情報で自身を更新する。 */
    public void refreshProfile(String username, String displayName, String profileUrl, Long followerCount, Long postCount) {
        this.username = username;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.followerCount = followerCount;
        this.postCount = postCount;
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public Platform getPlatform() {
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

    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
