package com.buzzanalysis.application.trend.dto;

import com.buzzanalysis.domain.platform.Platform;

/**
 * {@code GET /api/v1/trends} が返すハッシュタグ1件分。フロントエンドの{@code TrendHashtag}型に対応する。
 * {@code growthRate}は直近ウィンドウとベースラインウィンドウの比較による伸び率（%）。
 * ベースライン期間の出現が0件（新規急伸）の場合は100%を上限に丸める（無限大を返さないため）。
 */
public record TrendHashtagDto(String tag, Platform platform, String genre, int postCount, double growthRate) {
}
