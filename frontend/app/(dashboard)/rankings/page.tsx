"use client";

import { useState } from "react";
import { useRankings } from "@/lib/hooks/use-rankings";
import { Card } from "@/components/ui/card";
import { Tabs } from "@/components/ui/tabs";
import { Select } from "@/components/ui/select";
import { QueryState } from "@/components/dashboard/query-state";
import { PostCard } from "@/components/dashboard/post-card";
import { GENRE_OPTIONS, PLATFORM_OPTIONS, RANKING_TYPE_OPTIONS } from "@/lib/constants";
import type { Genre, Platform, RankingType } from "@/lib/types";

export default function RankingsPage() {
  const [type, setType] = useState<RankingType>("trending");
  const [platform, setPlatform] = useState<Platform | "">("");
  const [genre, setGenre] = useState<Genre | "">("");

  const rankingsQuery = useRankings({
    type,
    platform: platform || undefined,
    genre: genre || undefined,
  });

  return (
    <div className="space-y-6">
      <Card>
        <div className="flex flex-col gap-4">
          <Tabs items={[...RANKING_TYPE_OPTIONS]} value={type} onChange={setType} />
          <div className="flex flex-col gap-3 sm:flex-row">
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
        </div>
      </Card>

      <Card>
        <QueryState
          isLoading={rankingsQuery.isLoading}
          isError={rankingsQuery.isError}
          error={rankingsQuery.error}
          isEmpty={(rankingsQuery.data?.length ?? 0) === 0}
          emptyTitle="該当するランキングデータがありません"
          onRetry={() => rankingsQuery.refetch()}
        >
          <div className="space-y-2">
            {rankingsQuery.data?.map((item) => (
              <PostCard
                key={item.post.id}
                post={item.post}
                rank={item.rank}
                footer={
                  item.rankChange !== undefined && item.rankChange !== 0 ? (
                    <span
                      className={
                        item.rankChange > 0
                          ? "mt-1 inline-block text-xs font-medium text-emerald-600"
                          : "mt-1 inline-block text-xs font-medium text-red-500"
                      }
                    >
                      {item.rankChange > 0 ? "▲" : "▼"} {Math.abs(item.rankChange)}
                    </span>
                  ) : undefined
                }
              />
            ))}
          </div>
        </QueryState>
      </Card>
    </div>
  );
}
