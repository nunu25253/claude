import { useMutation, useQuery } from "@tanstack/react-query";
import { postsApi } from "@/lib/api";
import type { AnalyzePostRequest, PostSearchParams } from "@/lib/types";

export function useAnalyzePost() {
  return useMutation({
    mutationFn: (payload: AnalyzePostRequest) => postsApi.analyze(payload),
  });
}

export function usePostSearch(params: PostSearchParams, enabled = true) {
  return useQuery({
    queryKey: ["posts", "search", params],
    queryFn: () => postsApi.search(params),
    enabled,
  });
}

export function useBuzzScoreHistory(postId: string, enabled = true) {
  return useQuery({
    queryKey: ["posts", postId, "buzz-score-history"],
    queryFn: () => postsApi.buzzScoreHistory(postId),
    enabled,
  });
}

export function useScoreComparison(postId: string, enabled = true) {
  return useQuery({
    queryKey: ["posts", postId, "comparison"],
    queryFn: () => postsApi.scoreComparison(postId),
    enabled,
  });
}
