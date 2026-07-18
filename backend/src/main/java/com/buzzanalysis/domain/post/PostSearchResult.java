package com.buzzanalysis.domain.post;

import java.util.List;

/**
 * ページング済み投稿検索結果を表す値オブジェクト。
 *
 * @param content       検索結果の投稿一覧
 * @param page          現在のページ番号（0始まり）
 * @param size          1ページあたりの件数
 * @param totalElements 総件数
 */
public record PostSearchResult(List<Post> content, int page, int size, long totalElements) {

    public int totalPages() {
        return size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }
}
