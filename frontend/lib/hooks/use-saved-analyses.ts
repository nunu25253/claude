import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { savedAnalysesApi } from "@/lib/api";
import type { SetAlertThresholdRequest } from "@/lib/types";

const SAVED_ANALYSES_KEY = ["saved-analyses"];

export function useSavedAnalyses() {
  return useQuery({
    queryKey: SAVED_ANALYSES_KEY,
    queryFn: () => savedAnalysesApi.list(),
  });
}

export function useDeleteSavedAnalysis() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => savedAnalysesApi.remove(id),
    onSuccess: () => {
      // 削除後は一覧を再取得して最新状態に同期する
      queryClient.invalidateQueries({ queryKey: SAVED_ANALYSES_KEY });
    },
  });
}

export function useCreateSavedAnalysis() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: savedAnalysesApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: SAVED_ANALYSES_KEY });
    },
  });
}

export function useSetAlertThreshold() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: SetAlertThresholdRequest }) =>
      savedAnalysesApi.setAlertThreshold(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: SAVED_ANALYSES_KEY });
    },
  });
}
