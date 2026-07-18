package com.buzzanalysis.domain.normalization;

import com.buzzanalysis.domain.normalization.strategy.PlatformNormalizationStrategy;
import com.buzzanalysis.domain.post.Post;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * {@link PostNormalizer} の実装（Normalizerパターン）。{@link PostFieldMapper} で共通フィールドを写し取り、
 * 対応する {@link PlatformNormalizationStrategy} でプラットフォーム別の差異（メディア件数、再生数/シェア数が
 * 実測値として信頼できるか）を反映して {@link NormalizedPost} を完成させる。
 * フレームワーク非依存の純粋なドメインサービス。Strategy一覧はコンストラクタインジェクションで受け取り、
 * 走査順で最初に {@code supports()==true} を返したものを採用する
 * （汎用フォールバックのStrategyは呼び出し側でリストの末尾に配置すること）。
 */
public class DefaultPostNormalizer implements PostNormalizer {

    private final PostFieldMapper fieldMapper;
    private final List<PlatformNormalizationStrategy> strategies;

    public DefaultPostNormalizer(PostFieldMapper fieldMapper, List<PlatformNormalizationStrategy> strategies) {
        this.fieldMapper = Objects.requireNonNull(fieldMapper, "fieldMapper must not be null");
        this.strategies = Objects.requireNonNull(strategies, "strategies must not be null");
        if (strategies.isEmpty()) {
            throw new IllegalArgumentException("at least one PlatformNormalizationStrategy is required "
                    + "(a fallback DefaultNormalizationStrategy should always be registered)");
        }
    }

    @Override
    public NormalizedPost normalize(Post post) {
        PlatformNormalizationStrategy strategy = resolve(post);

        Long viewCount = strategy.isViewCountMeasured(post) ? post.getViewCount() : null;
        Long shareCount = strategy.isShareCountMeasured(post) ? post.getShareCount() : null;

        Set<String> unmeasuredMetrics = new HashSet<>();
        if (viewCount == null) {
            unmeasuredMetrics.add("viewCount");
        }
        if (shareCount == null) {
            unmeasuredMetrics.add("shareCount");
        }

        return fieldMapper.toBuilder(post)
                .mediaCount(strategy.mediaCount(post))
                .hasVideo(strategy.hasVideo(post))
                .viewCount(viewCount)
                .shareCount(shareCount)
                .engagementRate(calculateEngagementRate(post, viewCount))
                .unmeasuredMetrics(unmeasuredMetrics)
                .build();
    }

    private PlatformNormalizationStrategy resolve(Post post) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(post.getPlatform()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No PlatformNormalizationStrategy (not even a fallback) supports platform: " + post.getPlatform()));
    }

    /** いいね数・コメント数・再生数の「実測値」のみから算出する。再生数が未計測の場合は推定を行わずnullを返す。 */
    private Double calculateEngagementRate(Post post, Long measuredViewCount) {
        if (measuredViewCount == null || measuredViewCount == 0) {
            return null;
        }
        long likes = post.getLikeCount() == null ? 0L : post.getLikeCount();
        long comments = post.getCommentCount() == null ? 0L : post.getCommentCount();
        return (likes + comments) / (double) measuredViewCount;
    }
}
