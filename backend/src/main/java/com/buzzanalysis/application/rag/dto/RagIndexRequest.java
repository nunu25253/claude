package com.buzzanalysis.application.rag.dto;

import com.buzzanalysis.domain.rag.RagSourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** RAG索引登録APIのリクエストDTO（Phase16）。{@code sourceId}は任意（既存集約に紐付かない場合はnull可）。 */
public record RagIndexRequest(@NotNull RagSourceType sourceType, UUID sourceId,
                               @NotBlank @Size(max = 20000) String contentText) {
}
