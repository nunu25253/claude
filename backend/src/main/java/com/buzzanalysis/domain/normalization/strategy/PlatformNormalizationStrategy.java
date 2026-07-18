package com.buzzanalysis.domain.normalization.strategy;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;

/**
 * プラットフォームごとに異なる正規化ルールを吸収するStrategyインターフェース（Strategyパターン）。
 * 「メディア件数の数え方」「再生数/シェア数がそのプラットフォーム・投稿種別で公開情報として
 * 信頼できるか」は、SNSごとの仕様・利用規約（{@code docs/phases/00_sns_data_constraints.md} 参照）
 * によって異なるため、プラットフォーム単位で実装を差し替えられるようにしている。
 */
public interface PlatformNormalizationStrategy {

    /**
     * このStrategyが指定されたプラットフォームに対応しているかどうか。
     * {@code DefaultPostNormalizer} は、注入されたStrategy一覧を順に走査し、最初に {@code true} を
     * 返したものを採用する（フォールバック用の汎用Strategyは常に {@code true} を返し、リストの末尾に置く）。
     */
    boolean supports(Platform platform);

    /** 正規化されたメディア件数（カルーセルは画像枚数、単一メディア投稿は1）。 */
    int mediaCount(Post post);

    /** 動画を含む投稿かどうか。 */
    boolean hasVideo(Post post);

    /**
     * この投稿の {@code viewCount} が、当該プラットフォーム・投稿種別において
     * 公開情報として信頼できる実測値かどうか。falseの場合、Normalizerは値を破棄しnullとして扱う。
     */
    boolean isViewCountMeasured(Post post);

    /**
     * この投稿の {@code shareCount} が、当該プラットフォーム・投稿種別において
     * 公開情報として信頼できる実測値かどうか。falseの場合、Normalizerは値を破棄しnullとして扱う。
     */
    boolean isShareCountMeasured(Post post);
}
