import { useMutation, useQuery } from "@tanstack/react-query";
import { reportsApi } from "@/lib/api";
import type { ReportFormat } from "@/lib/types";

export function useReportHistory() {
  return useQuery({
    queryKey: ["reports", "history"],
    queryFn: () => reportsApi.history(),
  });
}

export function useGenerateReport() {
  return useMutation({
    mutationFn: ({ postId, format }: { postId: string; format: ReportFormat }) =>
      reportsApi.generate(postId, format),
  });
}
