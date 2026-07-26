import { apiClient } from "../api-client";
import type { AiImprovementRate } from "../types";

export const analyticsApi = {
  aiImprovementRate: () => apiClient.get<AiImprovementRate>("/analytics/ai-improvement-rate"),
};
