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
