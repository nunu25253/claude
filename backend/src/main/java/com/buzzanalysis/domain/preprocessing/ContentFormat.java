package com.buzzanalysis.domain.preprocessing;

/**
 * SNS横断で比較可能な、投稿のコンテンツ形式。プラットフォーム固有の {@code PostType}
 * （例: Instagramの「リール」、TikTokの「動画」）を、Phase7以降のランキング・比較で
 * 扱いやすい共通カテゴリへ再分類したもの。
 */
public enum ContentFormat {
    SHORT_VIDEO,
    LONG_VIDEO,
    SINGLE_IMAGE,
    MULTI_IMAGE_CAROUSEL,
    TEXT_ONLY
}
