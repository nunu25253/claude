import { useQuery } from "@tanstack/react-query";
import { competitorsApi } from "@/lib/api";

export function useCompetitorStats(accountId: string | null) {
  return useQuery({
    queryKey: ["competitors", "stats", accountId],
    queryFn: () => competitorsApi.getStats(accountId as string),
    enabled: !!accountId,
  });
}
