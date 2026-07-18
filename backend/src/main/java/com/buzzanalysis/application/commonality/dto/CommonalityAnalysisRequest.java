package com.buzzanalysis.application.commonality.dto;

import java.util.List;
import java.util.UUID;

/** 共通点分析APIのリクエストDTO（Phase8）。 */
public record CommonalityAnalysisRequest(List<UUID> postIds) {
}
