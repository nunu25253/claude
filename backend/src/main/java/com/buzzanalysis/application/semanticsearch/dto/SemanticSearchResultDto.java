package com.buzzanalysis.application.semanticsearch.dto;

import com.buzzanalysis.application.post.dto.PostDto;

/**
 * 意味検索の1件分の結果（Phase4）。
 *
 * @param post              マッチした投稿
 * @param similarity        コサイン類似度（0.0〜1.0、1.0に近いほど類似）
 * @param matchRatePercent  一致率（0〜100にクランプした表示用パーセンテージ）
 */
public record SemanticSearchResultDto(PostDto post, double similarity, double matchRatePercent) {
}
