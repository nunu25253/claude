"use client";

import dynamic from "next/dynamic";
import { useBuzzScoreHistory } from "@/lib/hooks/use-posts";
import { Card, CardHeader } from "@/components/ui/card";
import { ChartSkeleton } from "@/components/charts/chart-skeleton";

// rechartsは重量級のため、このセクションが実際に表示される時点まで読み込みを遅延させる
// (/posts/analyze, /saved, /team の初期バンドルサイズを削減するため)。
const BuzzScoreTrendChart = dynamic(
  () => import("@/components/charts/buzz-score-trend-chart").then((m) => m.BuzzScoreTrendChart),
  { ssr: false, loading: () => <ChartSkeleton height={220} /> },
);

export function BuzzScoreHistorySection({ postId }: { postId: string }) {
  const historyQuery = useBuzzScoreHistory(postId);
  const points = historyQuery.data ?? [];

  // 初回分析直後は履歴が1件のみでグラフとして意味を成さないため、2件以上溜まってから表示する。
  if (historyQuery.isLoading || historyQuery.isError || points.length < 2) {
    return null;
  }

  return (
    <Card>
      <CardHeader title="📈 BuzzScore推移" description="再分析を重ねるたびに推移が蓄積されます" />
      <BuzzScoreTrendChart data={points} />
    </Card>
  );
}
