"use client";

import { useBuzzScoreHistory } from "@/lib/hooks/use-posts";
import { Card, CardHeader } from "@/components/ui/card";
import { BuzzScoreTrendChart } from "@/components/charts/buzz-score-trend-chart";

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
