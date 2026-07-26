import { useQuery } from "@tanstack/react-query";
import { systemApi } from "@/lib/api";

// SNS公式API連携の設定状況はデプロイ時に固定される値のため(実行中に変わらない)、
// 通常のクエリより長いstaleTimeで無駄な再取得を避ける。
export function useDataMode() {
  return useQuery({
    queryKey: ["system", "dataMode"],
    queryFn: () => systemApi.getDataMode(),
    staleTime: 60 * 60 * 1000,
  });
}
