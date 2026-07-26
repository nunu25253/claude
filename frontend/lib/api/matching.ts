import { apiClient } from "../api-client";
import type { MatchingConditionRequest, MatchRateResult } from "../types";

export const matchingApi = {
  evaluate: (payload: MatchingConditionRequest, limit = 20) =>
    apiClient.post<MatchRateResult[]>(`/matching/evaluate?limit=${limit}`, payload),
};
