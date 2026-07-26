import { apiClient } from "../api-client";
import type { CreateSavedAnalysisRequest, SavedAnalysis, SetAlertThresholdRequest, ShareLink } from "../types";

export const savedAnalysesApi = {
  list: () => apiClient.get<SavedAnalysis[]>("/saved-analyses"),

  create: (payload: CreateSavedAnalysisRequest) =>
    apiClient.post<SavedAnalysis>("/saved-analyses", payload),

  remove: (id: string) => apiClient.delete<void>(`/saved-analyses/${id}`),

  setAlertThreshold: (id: string, payload: SetAlertThresholdRequest) =>
    apiClient.patch<SavedAnalysis>(`/saved-analyses/${id}/alert-threshold`, payload),

  createShareLink: (id: string) => apiClient.post<ShareLink>(`/saved-analyses/${id}/share`),

  revokeShareLink: (id: string) => apiClient.delete<void>(`/saved-analyses/${id}/share`),
};
