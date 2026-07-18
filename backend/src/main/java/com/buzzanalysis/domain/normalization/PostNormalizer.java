package com.buzzanalysis.domain.normalization;

import com.buzzanalysis.domain.post.Post;

/**
 * {@link Post} を {@link NormalizedPost} へ変換するユースケースを表すドメインサービス（Normalizerパターン）。
 * 実装は {@link PostFieldMapper}（共通フィールドの構造マッピング）と
 * {@link com.buzzanalysis.domain.normalization.strategy.PlatformNormalizationStrategy}
 * （プラットフォーム別の差異吸収、Strategyパターン）を組み合わせてオーケストレーションする。
 */
public interface PostNormalizer {

    NormalizedPost normalize(Post post);
}
