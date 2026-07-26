package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** 分析結果の保存（ブックマーク）リクエスト。 */
public record SaveAnalysisRequest(
        @NotNull UUID postId,
        @Size(max = 1000) String note
) {
}
