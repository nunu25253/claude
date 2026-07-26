package com.buzzanalysis.domain.platform;

/**
 * 対応SNSプラットフォームを表す列挙型。
 * 将来的にYouTube, Pinterest, Threads等を追加する際はここに定数を追加し、
 * {@link com.buzzanalysis.infrastructure.external.platform.PlatformFactory} に実装を登録するだけでよい設計とする。
 */
public enum Platform {
    INSTAGRAM,
    TIKTOK,
    X,
    YOUTUBE,
    PINTEREST,
    THREADS
}
