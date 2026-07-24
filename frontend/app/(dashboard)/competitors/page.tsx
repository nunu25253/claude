"use client";

import { useState } from "react";
import Image from "next/image";
import dynamic from "next/dynamic";
import { useCompetitorStats } from "@/lib/hooks/use-competitors";
import { Card, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { QueryState } from "@/components/dashboard/query-state";
import { EmptyState } from "@/components/ui/empty-state";
import { PostCard } from "@/components/dashboard/post-card";
import { ChartSkeleton } from "@/components/charts/chart-skeleton";
import { PostingTimeHeatmap } from "@/components/charts/posting-time-heatmap";
import { PlatformBadge } from "@/components/ui/badge";
import {
  formatCompactNumber,
  formatDurationSeconds,
  formatNumber,
} from "@/lib/utils";

// rechartsは重量級のため、このページの初期バンドルサイズを削減するために遅延読み込みする。
const EngagementTrendChart = dynamic(
  () => import("@/components/charts/engagement-trend-chart").then((m) => m.EngagementTrendChart),
  { ssr: false, loading: () => <ChartSkeleton /> },
);

export default function CompetitorsPage() {
  const [inputValue, setInputValue] = useState("");
  // 検索実行済みの accountId のみをクエリに渡す（入力中は自動検索しない）
  const [accountId, setAccountId] = useState<string | null>(null);

  const statsQuery = useCompetitorStats(accountId);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (inputValue.trim()) {
      setAccountId(inputValue.trim());
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <form onSubmit={handleSearch} className="flex flex-col gap-3 sm:flex-row">
          <input
            type="text"
            className="form-input"
            placeholder="アカウントID・ユーザー名を入力（例: @example_account）"
            value={inputValue}
            onChange={(e) => setInputValue(e.target.value)}
            aria-label="競合アカウントを検索"
          />
          <Button type="submit" className="sm:w-32">
            検索
          </Button>
        </form>
      </Card>

      {!accountId ? (
        <EmptyState
          icon="🔍"
          title="アカウントを検索してください"
          description="分析したい競合アカウントのIDまたはユーザー名を入力して検索すると、統計情報が表示されます。"
        />
      ) : (
        <QueryState
          isLoading={statsQuery.isLoading}
          isError={statsQuery.isError}
          error={statsQuery.error}
          onRetry={() => statsQuery.refetch()}
        >
          {statsQuery.data && <CompetitorStatsView stats={statsQuery.data} />}
        </QueryState>
      )}
    </div>
  );
}

function CompetitorStatsView({
  stats,
}: {
  stats: NonNullable<ReturnType<typeof useCompetitorStats>["data"]>;
}) {
  const metrics = [
    { label: "平均いいね数", value: formatCompactNumber(stats.avgLikeCount) },
    { label: "平均コメント数", value: formatCompactNumber(stats.avgCommentCount) },
    { label: "平均シェア数", value: formatCompactNumber(stats.avgShareCount) },
    { label: "投稿頻度", value: `週${stats.postFrequencyPerWeek.toFixed(1)}回` },
    { label: "平均動画時間", value: formatDurationSeconds(stats.avgVideoDurationSeconds) },
    { label: "平均文字数", value: `${formatNumber(stats.avgCharacterCount)}文字` },
  ];

  return (
    <div className="space-y-6">
      <Card className="flex flex-wrap items-center gap-4">
        <div className="relative flex h-14 w-14 items-center justify-center overflow-hidden rounded-full bg-slate-100 text-2xl">
          {stats.account.avatarUrl ? (
            // 外部SNSアバターは任意のドメインから配信されるため unoptimized で表示する。
            <Image
              src={stats.account.avatarUrl}
              alt={`${stats.account.displayName}のプロフィール画像`}
              fill
              unoptimized
              className="object-cover"
              sizes="56px"
            />
          ) : (
            "👤"
          )}
        </div>
        <div>
          <div className="flex items-center gap-2">
            <p className="font-semibold text-slate-900">{stats.account.displayName}</p>
            <PlatformBadge platform={stats.account.platform} />
          </div>
          <p className="text-sm text-slate-500">
            @{stats.account.handle} ・ フォロワー {formatCompactNumber(stats.account.followerCount)} ・
            投稿数 {formatCompactNumber(stats.account.postCount)}
          </p>
        </div>
      </Card>

      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-6">
        {metrics.map((m) => (
          <Card key={m.label} className="text-center">
            <p className="text-xs text-slate-500">{m.label}</p>
            <p className="mt-1 text-lg font-bold text-slate-900">{m.value}</p>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader title="エンゲージメント推移" description="平均エンゲージメント率の推移" />
          {stats.engagementTrend.length === 0 ? (
            <p className="py-8 text-center text-sm text-slate-500 dark:text-slate-400">データがありません</p>
          ) : (
            <EngagementTrendChart data={stats.engagementTrend} />
          )}
        </Card>
        <Card>
          <CardHeader title="投稿時間帯" description="曜日 × 時間帯の投稿分布" />
          {stats.postingTimeDistribution.length === 0 ? (
            <p className="py-8 text-center text-sm text-slate-500 dark:text-slate-400">データがありません</p>
          ) : (
            <PostingTimeHeatmap data={stats.postingTimeDistribution} />
          )}
        </Card>
      </div>

      <Card>
        <CardHeader title="伸びる投稿ランキング" description="このアカウントでよく伸びている投稿" />
        {stats.topGrowingPosts.length === 0 ? (
          <p className="py-8 text-center text-sm text-slate-500 dark:text-slate-400">データがありません</p>
        ) : (
          <div className="space-y-2">
            {stats.topGrowingPosts.map((post, idx) => (
              <PostCard key={post.id} post={post} rank={idx + 1} />
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
