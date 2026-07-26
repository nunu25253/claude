import { apiClient } from "../api-client";
import type { CommonalityAnalysisRequest, CommonalityAnalysisResult } from "../types";

export const commonalityApi = {
  analyze: (payload: CommonalityAnalysisRequest) =>
    apiClient.post<CommonalityAnalysisResult>("/commonality/analyze", payload),
};
