package com.buzzanalysis.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

/** 投稿URL分析リクエスト。 */
public record AnalyzePostRequest(@NotBlank String postUrl) {
}
