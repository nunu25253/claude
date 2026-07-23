"use client";

import { useState } from "react";
import { useDeleteSavedAnalysis, useSavedAnalyses } from "@/lib/hooks/use-saved-analyses";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { QueryState } from "@/components/dashboard/query-state";
import { PlatformBadge } from "@/components/ui/badge";
import { AnalysisResult } from "@/components/dashboard/analysis/analysis-result";
import { formatDateTime } from "@/lib/utils";
import { downloadCsv, toCsv } from "@/lib/csv";
import type { SavedAnalysis } from "@/lib/types";

function buildSavedAnalysesCsv(items: SavedAnalysis[]): string {
  const headers = [
    "保存日",
    "プラットフォーム",
    "アカウント",
    "キャプション",
    "投稿URL",
    "いいね数",
    "コメント数",
    "シェア数",
    "BuzzScore",
    "メモ",
  ];
  const rows = items.map((item) => [
    formatDateTime(item.createdAt),
    item.post.platform,
    item.post.accountHandle ?? item.post.authorName ?? "",
    item.post.caption ?? "",
    item.post.url ?? "",
    item.post.likeCount ?? 0,
    item.post.commentCount ?? 0,
    item.post.shareCount ?? 0,
    item.buzzScore?.totalScore ?? "",
    item.note ?? "",
  ]);
  return toCsv(headers, rows);
}

export default function SavedPage() {
  const savedQuery = useSavedAnalyses();
  const deleteMutation = useDeleteSavedAnalysis();
  const [expandedId, setExpandedId] = useState<string | null>(null);
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);

  const handleDelete = async (id: string) => {
    setPendingDeleteId(id);
    try {
      await deleteMutation.mutateAsync(id);
      if (expandedId === id) setExpandedId(null);
    } finally {
      setPendingDeleteId(null);
    }
  };

  const handleExportCsv = () => {
    if (!savedQuery.data || savedQuery.data.length === 0) return;
    const csv = buildSavedAnalysesCsv(savedQuery.data);
    const today = new Date().toISOString().slice(0, 10);
    downloadCsv(`saved-analyses-${today}.csv`, csv);
  };

  return (
    <div className="space-y-4">
      {(savedQuery.data?.length ?? 0) > 0 && (
        <div className="flex justify-end">
          <Button variant="secondary" onClick={handleExportCsv}>
            CSVエクスポート
          </Button>
        </div>
      )}
      <QueryState
        isLoading={savedQuery.isLoading}
        isError={savedQuery.isError}
        error={savedQuery.error}
        isEmpty={(savedQuery.data?.length ?? 0) === 0}
        emptyTitle="保存済みの分析結果がありません"
        emptyDescription="「投稿分析」で分析した結果を保存すると、ここに一覧表示されます。"
        onRetry={() => savedQuery.refetch()}
      >
        <div className="space-y-3">
          {savedQuery.data?.map((item) => {
            const isExpanded = expandedId === item.id;
            return (
              <Card key={item.id}>
                <div className="flex items-center gap-4">
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <PlatformBadge platform={item.post.platform} />
                      <span className="truncate text-sm font-medium text-slate-700">
                        {item.post.caption || "(キャプションなし)"}
                      </span>
                    </div>
                    <p className="mt-0.5 text-xs text-slate-400">
                      @{item.post.accountHandle ?? item.post.authorName} ・ 保存日 {formatDateTime(item.createdAt)}
                    </p>
                    {item.note && <p className="mt-1 text-xs text-slate-500">メモ: {item.note}</p>}
                  </div>
                  <div className="flex shrink-0 gap-2">
                    {item.analysis && item.buzzScore && (
                      <Button
                        variant="secondary"
                        onClick={() => setExpandedId(isExpanded ? null : item.id)}
                      >
                        {isExpanded ? "閉じる" : "詳細を見る"}
                      </Button>
                    )}
                    <Button
                      variant="danger"
                      onClick={() => handleDelete(item.id)}
                      isLoading={pendingDeleteId === item.id}
                    >
                      削除
                    </Button>
                  </div>
                </div>

                {isExpanded && item.analysis && item.buzzScore && (
                  <div className="mt-5 border-t border-slate-100 pt-5">
                    <AnalysisResult
                      result={{
                        post: item.post,
                        analysis: item.analysis,
                        buzzScore: item.buzzScore,
                        // 保存済み分析の一覧APIは類似投稿を返さないため空値で補う。
                        similarPosts: [],
                      }}
                      hideSaveAction
                    />
                  </div>
                )}
              </Card>
            );
          })}
        </div>
      </QueryState>
    </div>
  );
}
