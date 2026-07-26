import { apiClient } from "../api-client";
import type { RagQueryRequest, RagQueryResult } from "../types";

export const ragApi = {
  query: (payload: RagQueryRequest) => apiClient.post<RagQueryResult>("/rag/query", payload),
};
