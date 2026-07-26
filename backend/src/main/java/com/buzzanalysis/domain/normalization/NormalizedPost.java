package com.buzzanalysis.domain.normalization;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.PostType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * SNSごとに異なるデータ構造を統一した、プラットフォーム非依存の正規化済み投稿データ（Phase1の成果物）。
 * Phase2以降（前処理・Embedding生成・AI分析・ランキング等）は、{@link com.buzzanalysis.domain.post.Post}
 * を直接参照せず、この型を唯一の入力契約として利用する（他プラットフォームのデータ差異から後続処理を守る
 * Anti-Corruption Layerとしての役割）。
 *
 * <p><b>重要:</b> {@code viewCount} / {@code shareCount} は、プラットフォームの仕様上「公開情報として
 * 取得できない（=未計測）」場合は {@code null} のままとし、0や推定値で埋めない。未計測のフィールド名は
 * {@link #unmeasuredMetrics()} に列挙され、後続のAI分析が「実測値」と「推定値」を混同しないための
 * 手がかりとして使われる（AIによる推定はこのフェーズでは一切行わない）。</p>
 *
 * @param postId                元となる {@code Post} 集約のID
 * @param accountId             投稿者アカウントID
 * @param platform              プラットフォーム種別
 * @param postType              投稿種別（リール/画像/動画/カルーセル/テキスト）
 * @param url                   投稿の公開URL
 * @param publishedAt           投稿日時
 * @param authorName            投稿者名
 * @param rawText               キャプション/本文（未加工。絵文字・URL等の除去はPhase2で行う）
 * @param hashtags              収集時点で抽出済みのハッシュタグ一覧
 * @param likeCount             いいね数
 * @param commentCount          コメント数
 * @param viewCount             再生数（未計測の場合はnull）
 * @param shareCount            シェア数（未計測の場合はnull）
 * @param videoDurationSeconds  動画時間（秒）。動画を含まない投稿はnull
 * @param mediaCount            正規化されたメディア件数（カルーセルは画像枚数、単一メディア投稿は1）
 * @param hasVideo              動画を含む投稿かどうか
 * @param engagementRate        実測値のみから算出したエンゲージメント率（未計測でnullの場合あり。推定値は含まない）
 * @param postAgeInHours        正規化時点での投稿経過時間（時間）
 * @param unmeasuredMetrics     このプラットフォーム/投稿種別では取得不可・未計測のメトリクス名の集合（例: "viewCount"）
 */
public record NormalizedPost(
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

    public static Builder builder() {
        return new Builder();
    }

    /** Builderパターンによる段階的な組み立て（Mapperが共通フィールドを、Strategyがプラットフォーム別フィールドを埋める）。 */
    public static final class Builder {
        private UUID postId;
        private UUID accountId;
        private Platform platform;
        private PostType postType;
        private String url;
        private OffsetDateTime publishedAt;
        private String authorName;
        private String rawText;
        private List<String> hashtags = List.of();
        private Long likeCount;
        private Long commentCount;
        private Long viewCount;
        private Long shareCount;
        private Integer videoDurationSeconds;
        private int mediaCount = 1;
        private boolean hasVideo;
        private Double engagementRate;
        private long postAgeInHours;
        private Set<String> unmeasuredMetrics = Set.of();

        public Builder postId(UUID postId) {
            this.postId = postId;
            return this;
        }

        public Builder accountId(UUID accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder platform(Platform platform) {
            this.platform = platform;
            return this;
        }

        public Builder postType(PostType postType) {
            this.postType = postType;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder publishedAt(OffsetDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public Builder authorName(String authorName) {
            this.authorName = authorName;
            return this;
        }

        public Builder rawText(String rawText) {
            this.rawText = rawText;
            return this;
        }

        public Builder hashtags(List<String> hashtags) {
            this.hashtags = hashtags == null ? List.of() : hashtags;
            return this;
        }

        public Builder likeCount(Long likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public Builder commentCount(Long commentCount) {
            this.commentCount = commentCount;
            return this;
        }

        public Builder viewCount(Long viewCount) {
            this.viewCount = viewCount;
            return this;
        }

        public Builder shareCount(Long shareCount) {
            this.shareCount = shareCount;
            return this;
        }

        public Builder videoDurationSeconds(Integer videoDurationSeconds) {
            this.videoDurationSeconds = videoDurationSeconds;
            return this;
        }

        public Builder mediaCount(int mediaCount) {
            this.mediaCount = mediaCount;
            return this;
        }

        public Builder hasVideo(boolean hasVideo) {
            this.hasVideo = hasVideo;
            return this;
        }

        public Builder engagementRate(Double engagementRate) {
            this.engagementRate = engagementRate;
            return this;
        }

        public Builder postAgeInHours(long postAgeInHours) {
            this.postAgeInHours = postAgeInHours;
            return this;
        }

        public Builder unmeasuredMetrics(Set<String> unmeasuredMetrics) {
            this.unmeasuredMetrics = unmeasuredMetrics == null ? Set.of() : unmeasuredMetrics;
            return this;
        }

        public NormalizedPost build() {
            return new NormalizedPost(postId, accountId, platform, postType, url, publishedAt, authorName,
                    rawText, hashtags, likeCount, commentCount, viewCount, shareCount, videoDurationSeconds,
                    mediaCount, hasVideo, engagementRate, postAgeInHours, unmeasuredMetrics);
        }
    }
}
