package com.buzzanalysis.domain.post;

import com.buzzanalysis.domain.platform.Platform;

/**
 * 投稿検索条件を表す値オブジェクト（フレームワーク非依存）。
 * ページング/ソートの実際の適用はinfrastructure層に委ねる。
 *
 * @param keyword    キャプション全文検索キーワード（nullable）
 * @param hashtag    ハッシュタグ完全一致（nullable, "#"なし）
 * @param accountId  アカウントID（nullable）
 * @param platform   プラットフォームフィルタ（nullable）
 * @param page       0始まりのページ番号
 * @param size       1ページあたりの件数
 * @param sortBy     ソート対象フィールド名（likeCount, publishedAt等）
 * @param ascending  昇順ならtrue
 */
public record PostSearchCriteria(
        String keyword,
        String hashtag,
        java.util.UUID accountId,
        Platform platform,
        int page,
        int size,
        String sortBy,
        boolean ascending
) {
    public PostSearchCriteria {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "publishedAt";
        }
    }
}
