package com.buzzanalysis.domain.normalization.strategy;

import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;

/**
 * まだ専用Strategyが用意されていないプラットフォーム（現時点ではYouTube / Threads / Pinterest。
 * これらはデータ収集アダプタ自体が未実装。{@code docs/phases/00_sns_data_constraints.md} 参照）向けの
 * フォールバック実装。データ収集アダプタが追加された際は、専用のStrategy実装に差し替えることを推奨する
 * （TODO: YouTube/Threads収集アダプタ実装時に専用Strategyへ置き換える）。
 *
 * <p>保守的な方針として、再生数・シェア数はいずれも「未計測」として扱う（実測値と断定できないものを
 * 実測値として扱わないため）。</p>
 */
public class DefaultNormalizationStrategy implements PlatformNormalizationStrategy {

    @Override
    public boolean supports(Platform platform) {
        // 他のどのStrategyにも該当しなかった場合の汎用フォールバックとして、常にtrueを返す。
        // DefaultPostNormalizerの走査順で必ず最後に配置すること。
        return true;
    }

    @Override
    public int mediaCount(Post post) {
        return post.getImageCount() != null && post.getImageCount() > 0 ? post.getImageCount() : 1;
    }

    @Override
    public boolean hasVideo(Post post) {
        return post.getVideoDurationSeconds() != null;
    }

    @Override
    public boolean isViewCountMeasured(Post post) {
        return false;
    }

    @Override
    public boolean isShareCountMeasured(Post post) {
        return false;
    }
}
