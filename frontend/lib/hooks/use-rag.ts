import { useMutation } from "@tanstack/react-query";
import { ragApi } from "@/lib/api";
import type { RagQueryRequest } from "@/lib/types";

export function useQueryRagAssistant() {
  return useMutation({
    mutationFn: (payload: RagQueryRequest) => ragApi.query(payload),
  });
}
