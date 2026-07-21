import type { BuzzScoreResult, Post, PostAnalysis } from "./post";

// バックエンドのSavedAnalysisDetailDtoに対応。投稿がAI分析未実施の場合はanalysis/buzzScoreがnull。
export interface SavedAnalysis {
  id: string;
  note?: string;
  createdAt: string;
  post: Post;
  analysis: PostAnalysis | null;
  buzzScore: BuzzScoreResult | null;
}

export interface CreateSavedAnalysisRequest {
  postId: string;
  note?: string;
}
