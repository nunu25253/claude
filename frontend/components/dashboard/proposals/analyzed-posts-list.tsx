"use client";

import { useMemo, useState } from "react";
import { useSavedAnalyses } from "@/lib/hooks/use-saved-analyses";
import { Card, CardHeader } from "@/components/ui/card";
import { QueryState } from "@/components/dashboard/query-state";
import { PlatformBadge } from "@/components/ui/badge";
import { formatDateTime } from "@/lib/utils";

type SortKey = "createdAt" | "buzzScore" | "platform";

const SORT_OPTIONS: { value: SortKey; label: string }[] = [
  { value: "createdAt", label: "保存日" },
  { value: "buzzScore", label: "BuzzScore" },
  { value: "platform", label: "プラットフォーム" },
];

/**
 * AI企画生成フォームは投稿IDを直接入力する仕様だが、投稿ID(UUID)を確認できる場所が
 * どこにも無く入力手段が事実上無かったため、分析済み(保存済み)の投稿を並び替え可能な
 * 一覧として表示し、各行から投稿IDをコピーできるようにする。
 */
export function AnalyzedPostsList() {
  const savedQuery = useSavedAnalyses();
  const [sortKey, setSortKey] = useState<SortKey>("createdAt");
  const [sortDesc, setSortDesc] = useState(true);
  const [copiedId, setCopiedId] = useState<string | null>(null);

  const sorted = useMemo(() => {
    const items = [...(savedQuery.data ?? [])];
    items.sort((a, b) => {
      let cmp = 0;
      if (sortKey === "createdAt") {
        cmp = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
      } else if (sortKey === "buzzScore") {
        cmp = (a.buzzScore?.totalScore ?? -1) - (b.buzzScore?.totalScore ?? -1);
      } else {
        cmp = a.post.platform.localeCompare(b.post.platform);
      }
      return sortDesc ? -cmp : cmp;
    });
    return items;
  }, [savedQuery.data, sortKey, sortDesc]);

  const handleCopy = async (postId: string) => {
    await navigator.clipboard.writeText(postId);
    setCopiedId(postId);
    setTimeout(() => setCopiedId((current) => (current === postId ? null : current)), 2000);
  };

  return (
    <Card>
      <CardHeader
        title="分析済みの投稿"
        description="投稿IDをコピーして、上のフォームに貼り付けてください。"
      />
      <div className="mb-3 flex items-center gap-2 text-sm">
        <span className="text-slate-500">並び替え:</span>
        {SORT_OPTIONS.map((opt) => (
          <button
            key={opt.value}
            type="button"
            onClick={() =>
              setSortKey((current) => {
                if (current === opt.value) {
                  setSortDesc((d) => !d);
                } else {
                  setSortDesc(true);
                }
                return opt.value;
              })
            }
            className={`rounded-full border px-3 py-1 text-xs font-medium transition ${
              sortKey === opt.value
                ? "border-brand-300 bg-brand-50 text-brand-700"
                : "border-slate-200 bg-white text-slate-500 hover:bg-slate-50"
            }`}
          >
            {opt.label}
            {sortKey === opt.value && (sortDesc ? " ↓" : " ↑")}
          </button>
        ))}
      </div>

      <QueryState
        isLoading={savedQuery.isLoading}
        isError={savedQuery.isError}
        error={savedQuery.error}
        isEmpty={sorted.length === 0}
        emptyTitle="分析済みの投稿がありません"
        emptyDescription="「投稿分析」で分析し保存すると、ここに一覧表示されます。"
        onRetry={() => savedQuery.refetch()}
      >
        <div className="divide-y divide-slate-100">
          {sorted.map((item) => (
            <div key={item.id} className="flex items-center gap-3 py-2.5">
              <div className="min-w-0 flex-1">
                <div className="flex items-center gap-2">
                  <PlatformBadge platform={item.post.platform} />
                  <span className="truncate text-sm text-slate-700">
                    {item.post.caption || "(キャプションなし)"}
                  </span>
                </div>
                <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-0.5 text-xs text-slate-400">
                  <span>
                    {item.buzzScore ? `BuzzScore ${Math.round(item.buzzScore.totalScore)} ・ ` : ""}
                    保存日 {formatDateTime(item.createdAt)}
                  </span>
                  <code className="rounded bg-slate-100 px-1.5 py-0.5 font-mono text-slate-500">
                    {item.post.id}
                  </code>
                </div>
              </div>
              <button
                type="button"
                onClick={() => handleCopy(item.post.id)}
                className="shrink-0 rounded-lg border border-slate-300 bg-white px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-50"
              >
                {copiedId === item.post.id ? "コピーしました" : "IDをコピー"}
              </button>
            </div>
          ))}
        </div>
      </QueryState>
    </Card>
  );
}
