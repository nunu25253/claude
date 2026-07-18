package com.buzzanalysis.domain.platform;

import com.buzzanalysis.domain.post.PostType;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 各SNSの公式APIから取得した投稿の生データを表す値オブジェクト。
 * {@link SocialPlatform} 実装はこの型を返し、application層がドメインの {@code Post} に変換する。
 * 非公開指標（インプレッション・リーチ・保存数等）は含めない。
 *
 * @param externalId            プラットフォーム側の投稿ID
 * @param url                   投稿の公開URL
 * @param publishedAt           投稿日時
 * @param authorName            投稿者名（ユーザー名）
 * @param caption               キャプション本文
 * @param hashtags              キャプションから抽出したハッシュタグ一覧
 * @param likeCount             いいね数（公開されている場合）
 * @param commentCount          コメント数（公開されている場合）
 * @param viewCount             再生数（動画系のみ、公開されている場合）
 * @param shareCount            シェア数（取得可能な場合のみ）
 * @param videoDurationSeconds  動画の長さ（秒）。画像投稿はnull
 * @param imageCount            画像枚数（カルーセル/画像投稿）
 * @param postType              正規化された投稿種別
 */
public record FetchedPostData(
        String externalId,
        String url,
        OffsetDateTime publishedAt,
        String authorName,
        String caption,
        List<String> hashtags,
        Long likeCount,
        Long commentCount,
        Long viewCount,
        Long shareCount,
        Integer videoDurationSeconds,
        Integer imageCount,
        PostType postType
) {
}
