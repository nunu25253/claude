package com.buzzanalysis.domain.platform;

/**
 * 各SNSの公式APIから取得したアカウントの公開プロフィール情報を表す値オブジェクト。
 *
 * @param externalAccountId プラットフォーム側のアカウントID
 * @param username          ユーザー名（@handle）
 * @param displayName       表示名
 * @param profileUrl        プロフィールページURL
 * @param followerCount     フォロワー数（公開されている場合）
 * @param postCount         投稿数（公開されている場合）
 */
public record FetchedAccountData(
        String externalAccountId,
        String username,
        String displayName,
        String profileUrl,
        Long followerCount,
        Long postCount
) {
}
