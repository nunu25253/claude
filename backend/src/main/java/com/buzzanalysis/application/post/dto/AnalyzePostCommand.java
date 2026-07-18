package com.buzzanalysis.application.post.dto;

/**
 * 投稿URL分析ユースケースの入力コマンド。
 *
 * @param postUrl 分析対象の投稿URL（Instagram/TikTok/X等の公開投稿URL）
 */
public record AnalyzePostCommand(String postUrl) {
}
