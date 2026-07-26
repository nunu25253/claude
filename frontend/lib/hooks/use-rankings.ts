import { useQuery } from "@tanstack/react-query";
import { rankingsApi } from "@/lib/api";
import type { RankingQueryParams } from "@/lib/types";

export function useRankings(params: RankingQueryParams) {
  return useQuery({
    queryKey: ["rankings", params],
    queryFn: () => rankingsApi.list(params),
  });
}
