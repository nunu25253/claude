import { useQuery } from "@tanstack/react-query";
import { sharedAnalysisApi } from "@/lib/api";

export function useSharedAnalysis(token: string) {
  return useQuery({
    queryKey: ["shared", token],
    queryFn: () => sharedAnalysisApi.get(token),
    retry: false,
  });
}
