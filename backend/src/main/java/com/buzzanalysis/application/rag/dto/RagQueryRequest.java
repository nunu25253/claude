package com.buzzanalysis.application.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** RAG質問応答APIのリクエストDTO（Phase16）。{@code topK}省略時は既定値を使用する。 */
public record RagQueryRequest(@NotBlank @Size(max = 1000) String question, Integer topK) {
}
