import { apiClient } from "../api-client";
import type { SavedAnalysis } from "../types";

/** 共有トークン経由の分析結果取得(未ログインでもアクセス可能な公開エンドポイント)。 */
export const sharedAnalysisApi = {
  get: (token: string) => apiClient.get<SavedAnalysis>(`/shared/${token}`),
};
