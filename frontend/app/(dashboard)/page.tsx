"use client";

import Link from "next/link";
import { useSavedAnalyses } from "@/lib/hooks/use-saved-analyses";
import { useRankings } from "@/lib/hooks/use-rankings";
import { KpiCard } from "@/components/dashboard/kpi-card";
import { PostCard } from "@/components/dashboard/post-card";
import { Card, CardHeader } from "@/components/ui/card";
import { QueryState } from "@/components/dashboard/query-state";
import { formatCompactNumber } from "@/lib/utils";

export default function HomePage() {
  // サマリーKPI用に保存済み分析一覧を取得（総分析数の算出に利用）
  const savedQuery = useSavedAnalyses();
  // 急上昇投稿プレビュー用にトレンドランキングを取得
  const trendingQuery = useRankings({ type: "trending" });

  const savedAnalyses = savedQuery.data ?? [];
  const totalAnalyses = savedAnalyses.length;
  const recentAnalyses = [...savedAnalyses]
    .sort((a, b) => new Date(b.savedAt).getTime() - new Date(a.savedAt).getTime())
    .slice(0, 5);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <KpiCard label="総分析数" icon="🧠" value={formatCompactNumber(totalAnalyses)} />
        <KpiCard
          label="急上昇投稿数"
          icon="📈"
          value={formatCompactNumber(trendingQuery.data?.length ?? 0)}
        />
        <KpiCard
          label="保存済み分析"
          icon="🔖"
          value={formatCompactNumber(savedAnalyses.length)}
        />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader
            title="直近の分析"
            description="最近保存した投稿分析"
            action={
              <Link href="/saved" className="text-sm font-medium text-brand-600 hover:underline">
                すべて見る
              </Link>
            }
          />
          <QueryState
            isLoading={savedQuery.isLoading}
            isError={savedQuery.isError}
            error={savedQuery.error}
            isEmpty={recentAnalyses.length === 0}
            emptyTitle="まだ分析結果がありません"
            emptyDescription="「投稿分析」から投稿URLを分析すると、ここに表示されます。"
            onRetry={() => savedQuery.refetch()}
          >
            <div className="space-y-2">
              {recentAnalyses.map((item) => (
                <PostCard key={item.id} post={item.post} />
              ))}
            </div>
          </QueryState>
        </Card>

        <Card>
          <CardHeader
            title="急上昇投稿"
            description="いま伸びている投稿のプレビュー"
            action={
              <Link href="/rankings" className="text-sm font-medium text-brand-600 hover:underline">
                ランキングを見る
              </Link>
            }
          />
          <QueryState
            isLoading={trendingQuery.isLoading}
            isError={trendingQuery.isError}
            error={trendingQuery.error}
            isEmpty={(trendingQuery.data?.length ?? 0) === 0}
            emptyTitle="急上昇中の投稿がまだありません"
            onRetry={() => trendingQuery.refetch()}
          >
            <div className="space-y-2">
              {trendingQuery.data?.slice(0, 5).map((item) => (
                <PostCard key={item.post.id} post={item.post} rank={item.rank} />
              ))}
            </div>
          </QueryState>
        </Card>
      </div>
    </div>
  );
}
