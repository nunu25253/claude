import { apiClient, toQueryParams } from "../api-client";
import type {
  AnalyzePostRequest,
  AnalyzePostResponse,
  BuzzScoreHistoryPoint,
  Page,
  Post,
  PostScoreComparison,
  PostSearchParams,
} from "../types";

export const postsApi = {
  analyze: (payload: AnalyzePostRequest) =>
    apiClient.post<AnalyzePostResponse>("/posts/analyze", payload),

  search: (params: PostSearchParams) =>
    apiClient.get<Page<Post>>("/posts/search", { params: toQueryParams(params) }),

  buzzScoreHistory: (postId: string) =>
    apiClient.get<BuzzScoreHistoryPoint[]>(`/posts/${postId}/buzz-score-history`),

  scoreComparison: (postId: string) =>
    apiClient.get<PostScoreComparison>(`/posts/${postId}/comparison`),
};
