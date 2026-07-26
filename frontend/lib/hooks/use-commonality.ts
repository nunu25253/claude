import { useMutation } from "@tanstack/react-query";
import { commonalityApi } from "@/lib/api";
import type { CommonalityAnalysisRequest } from "@/lib/types";

export function useAnalyzeCommonality() {
  return useMutation({
    mutationFn: (payload: CommonalityAnalysisRequest) => commonalityApi.analyze(payload),
  });
}
