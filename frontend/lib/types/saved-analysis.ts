import type { BuzzScoreResult, Post, PostAnalysis } from "./post";

// バックエンドのSavedAnalysisDetailDtoに対応。投稿がAI分析未実施の場合はanalysis/buzzScoreがnull。
// alertThreshold未設定(アラート無効)の場合はnull、設定済みで未通知ならalertTriggeredAtがnull。
export interface SavedAnalysis {
  id: string;
  note?: string;
  createdAt: string;
  post: Post;
  analysis: PostAnalysis | null;
  buzzScore: BuzzScoreResult | null;
  alertThreshold: number | null;
  alertTriggeredAt: string | null;
}

export interface CreateSavedAnalysisRequest {
  postId: string;
  note?: string;
}

export interface SetAlertThresholdRequest {
  threshold: number | null;
}
