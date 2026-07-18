import { apiClient } from "../api-client";
import type { CompetitorStats, Page, Post, PostSearchParams } from "../types";

export const competitorsApi = {
  getStats: (accountId: string) =>
    apiClient.get<CompetitorStats>(`/competitors/${accountId}/stats`),

  // アカウント検索は投稿検索APIの account パラメータを流用してアカウント候補を引く想定
  searchAccountPosts: (params: PostSearchParams) =>
    apiClient.get<Page<Post>>("/posts/search", { params }),
};
