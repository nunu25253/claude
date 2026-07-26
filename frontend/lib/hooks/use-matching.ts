import { useMutation } from "@tanstack/react-query";
import { matchingApi } from "@/lib/api";
import type { MatchingConditionRequest } from "@/lib/types";

export function useEvaluateMatching() {
  return useMutation({
    mutationFn: (payload: MatchingConditionRequest) => matchingApi.evaluate(payload),
  });
}
