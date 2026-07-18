import type { Post, PostAnalysis } from "./post";

export interface SavedAnalysis {
  id: string;
  post: Post;
  analysis: PostAnalysis;
  savedAt: string;
  memo?: string;
}

export interface CreateSavedAnalysisRequest {
  postId: string;
  analysisId: string;
  memo?: string;
}
