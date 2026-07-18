package com.buzzanalysis.infrastructure.persistence.entity;

/**
 * JPAエンティティ用のプラットフォーム列挙型。ドメインの {@link com.buzzanalysis.domain.platform.Platform}
 * とは意図的に分離し、永続化層がドメインモデルに直接依存しないようにする。
 */
public enum PlatformEnum {
    INSTAGRAM,
    TIKTOK,
    X,
    YOUTUBE,
    PINTEREST,
    THREADS
}
