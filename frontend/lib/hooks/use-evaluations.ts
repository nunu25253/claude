import { useMutation } from "@tanstack/react-query";
import { evaluationsApi } from "@/lib/api";
import type { PostEvaluationRequest } from "@/lib/types";

export function useEvaluatePost() {
  return useMutation({
    mutationFn: (payload: PostEvaluationRequest) => evaluationsApi.evaluate(payload),
  });
}
