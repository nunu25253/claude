package com.buzzanalysis.application.commonality.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/** 共通点分析APIのリクエストDTO（Phase8）。 */
public record CommonalityAnalysisRequest(@NotEmpty List<UUID> postIds) {
}
