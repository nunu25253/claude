"use client";

import { useState } from "react";
import { useTrends } from "@/lib/hooks/use-trends";
import { Card, CardHeader } from "@/components/ui/card";
import { Select } from "@/components/ui/select";
import { PlatformBadge } from "@/components/ui/badge";
import { PostCard } from "@/components/dashboard/post-card";
import { QueryState } from "@/components/dashboard/query-state";
import { GENRE_OPTIONS, PLATFORM_OPTIONS } from "@/lib/constants";
import { formatCompactNumber } from "@/lib/utils";
import type { Genre, Platform } from "@/lib/types";

export default function TrendPage() {
  const [platform, setPlatform] = useState<Platform | "">("");
  const [genre, setGenre] = useState<Genre | "">("");

  const trendsQuery = useTrends({
    platform: platform || undefined,
    genre: genre || undefined,
  });

  const hashtags = trendsQuery.data?.hashtags ?? [];
  const posts = trendsQuery.data?.posts ?? [];

  return (
    <div className="space-y-6">
      <Card>
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
          <p className="text-sm font-medium text-slate-600">絞り込み:</p>
          <Select
            options={PLATFORM_OPTIONS}
            placeholder="すべてのSNS"
            value={platform}
            onChange={(e) => setPlatform(e.target.value as Platform | "")}
            className="sm:w-48"
            aria-label="SNSで絞り込み"
          />
          <Select
            options={GENRE_OPTIONS}
            placeholder="すべてのジャンル"
            value={genre}
            onChange={(e) => setGenre(e.target.value as Genre | "")}
            className="sm:w-48"
            aria-label="ジャンルで絞り込み"
          />
        </div>
      </Card>

      <QueryState
        isLoading={trendsQuery.isLoading}
        isError={trendsQuery.isError}
        error={trendsQuery.error}
        onRetry={() => trendsQuery.refetch()}
      >
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
          <Card className="lg:col-span-1">
            <CardHeader title="トレンドハッシュタグ" description="伸び率が高い順" />
            {hashtags.length === 0 ? (
              <p className="py-8 text-center text-sm text-slate-400">該当するハッシュタグがありません</p>
            ) : (
              <ul className="space-y-2">
                {hashtags.map((h) => (
                  <li
                    key={`${h.platform}-${h.tag}`}
                    className="flex items-center justify-between gap-2 rounded-lg border border-slate-100 px-3 py-2"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium text-slate-800">#{h.tag}</p>
                      <div className="mt-1 flex items-center gap-1.5">
                        <PlatformBadge platform={h.platform} />
                        <span className="text-xs text-slate-400">
                          {formatCompactNumber(h.postCount)}件
                        </span>
                      </div>
                    </div>
                    <span
                      className={
                        h.growthRate >= 0
                          ? "shrink-0 text-sm font-semibold text-emerald-600"
                          : "shrink-0 text-sm font-semibold text-red-500"
                      }
                    >
                      {h.growthRate >= 0 ? "+" : ""}
                      {h.growthRate.toFixed(0)}%
                    </span>
                  </li>
                ))}
              </ul>
            )}
          </Card>

          <Card className="lg:col-span-2">
            <CardHeader title="トレンド投稿一覧" description="いま話題の投稿" />
            {posts.length === 0 ? (
              <p className="py-8 text-center text-sm text-slate-400">該当する投稿がありません</p>
            ) : (
              <div className="space-y-2">
                {posts.map((post) => (
                  <PostCard key={post.id} post={post} />
                ))}
              </div>
            )}
          </Card>
        </div>
      </QueryState>
    </div>
  );
}
