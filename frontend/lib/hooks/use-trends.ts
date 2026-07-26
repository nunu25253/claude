import { useQuery } from "@tanstack/react-query";
import { trendsApi } from "@/lib/api";
import type { TrendQueryParams } from "@/lib/types";

export function useTrends(params: TrendQueryParams) {
  return useQuery({
    queryKey: ["trends", params],
    queryFn: () => trendsApi.get(params),
  });
}
