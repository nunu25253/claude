import { apiClient } from "../api-client";
import type { ContentEvaluation, PostEvaluationRequest } from "../types";

export const evaluationsApi = {
  evaluate: (payload: PostEvaluationRequest) =>
    apiClient.post<ContentEvaluation>("/evaluations", payload),
};
