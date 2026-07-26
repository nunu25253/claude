package com.buzzanalysis.application.post.dto;

import java.util.List;

/** ページング済み投稿検索結果のDTO。 */
public record PostSearchResultDto(List<PostDto> content, int page, int size, long totalElements, int totalPages) {
}
