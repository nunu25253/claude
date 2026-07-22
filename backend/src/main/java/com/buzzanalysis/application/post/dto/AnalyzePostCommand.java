package com.buzzanalysis.application.post.dto;

import java.util.UUID;

/**
 * 投稿URL分析ユースケースの入力コマンド。
 *
 * @param postUrl           分析対象の投稿URL（Instagram/TikTok/X等の公開投稿URL）
 * @param requestingUserId  分析を要求したユーザーのID（日次利用上限のカウントに使う）
 */
public record AnalyzePostCommand(String postUrl, UUID requestingUserId) {
}
