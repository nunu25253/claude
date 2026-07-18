import { apiClient } from "../api-client";
import type {
  GeneratedReport,
  ReportFormat,
  ReportHistoryItem,
} from "../types";

export const reportsApi = {
  generate: (postId: string, format: ReportFormat) =>
    apiClient.post<GeneratedReport>(
      `/reports/${postId}/generate`,
      undefined,
      { params: { format } },
    ),

  history: () => apiClient.get<ReportHistoryItem[]>("/reports"),
};
