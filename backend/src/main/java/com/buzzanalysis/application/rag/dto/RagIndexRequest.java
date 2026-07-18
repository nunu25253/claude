package com.buzzanalysis.application.rag.dto;

import com.buzzanalysis.domain.rag.RagSourceType;

import java.util.UUID;

/** RAG索引登録APIのリクエストDTO（Phase16）。{@code sourceId}は任意（既存集約に紐付かない場合はnull可）。 */
public record RagIndexRequest(RagSourceType sourceType, UUID sourceId, String contentText) {
}
