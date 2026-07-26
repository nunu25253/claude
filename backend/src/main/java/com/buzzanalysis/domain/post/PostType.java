package com.buzzanalysis.domain.post;

/**
 * 投稿種別。SNSごとに呼び方は異なるが（例: Instagramの「リール」、TikTokの「動画」）、
 * ドメイン内では共通の種別に正規化して扱う。
 */
public enum PostType {
    REEL,
    IMAGE,
    VIDEO,
    CAROUSEL,
    TEXT
}
