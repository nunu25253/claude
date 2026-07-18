import { apiClient } from "../api-client";
import type { CreateSavedAnalysisRequest, SavedAnalysis } from "../types";

export const savedAnalysesApi = {
  list: () => apiClient.get<SavedAnalysis[]>("/saved-analyses"),

  create: (payload: CreateSavedAnalysisRequest) =>
    apiClient.post<SavedAnalysis>("/saved-analyses", payload),

  remove: (id: string) => apiClient.delete<void>(`/saved-analyses/${id}`),
};
