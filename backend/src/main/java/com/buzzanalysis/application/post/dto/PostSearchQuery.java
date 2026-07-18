package com.buzzanalysis.application.post.dto;

import com.buzzanalysis.domain.platform.Platform;

import java.util.UUID;

/** 投稿検索ユースケースの入力クエリ。 */
public record PostSearchQuery(
        String keyword,
        String hashtag,
        UUID accountId,
        Platform platform,
        int page,
        int size,
        String sortBy,
        boolean ascending
) {
}
