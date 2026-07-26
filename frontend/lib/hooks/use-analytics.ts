import { useQuery } from "@tanstack/react-query";
import { analyticsApi } from "@/lib/api";

export function useAiImprovementRate() {
  return useQuery({
    queryKey: ["analytics", "ai-improvement-rate"],
    queryFn: () => analyticsApi.aiImprovementRate(),
  });
}
