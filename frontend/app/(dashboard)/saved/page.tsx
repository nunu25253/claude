"use client";

import { useState } from "react";
import { useDeleteSavedAnalysis, useSavedAnalyses } from "@/lib/hooks/use-saved-analyses";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { QueryState } from "@/components/dashboard/query-state";
import { PlatformBadge } from "@/components/ui/badge";
import { BuzzScoreBadge } from "@/components/dashboard/buzz-score-badge";
import { AnalysisResult } from "@/components/dashboard/analysis/analysis-result";
import { formatDateTime } from "@/lib/utils";

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

  return (
    <div className="space-y-4">
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
                  <BuzzScoreBadge score={item.analysis.buzzScore.total} size="sm" />
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <PlatformBadge platform={item.post.platform} />
                      <span className="truncate text-sm font-medium text-slate-700">
                        {item.post.caption || "(キャプションなし)"}
                      </span>
                    </div>
                    <p className="mt-0.5 text-xs text-slate-400">
                      @{item.post.accountHandle} ・ 保存日 {formatDateTime(item.savedAt)}
                    </p>
                    {item.memo && <p className="mt-1 text-xs text-slate-500">メモ: {item.memo}</p>}
                  </div>
                  <div className="flex shrink-0 gap-2">
                    <Button
                      variant="secondary"
                      onClick={() => setExpandedId(isExpanded ? null : item.id)}
                    >
                      {isExpanded ? "閉じる" : "詳細を見る"}
                    </Button>
                    <Button
                      variant="danger"
                      onClick={() => handleDelete(item.id)}
                      isLoading={pendingDeleteId === item.id}
                    >
                      削除
                    </Button>
                  </div>
                </div>

                {isExpanded && (
                  <div className="mt-5 border-t border-slate-100 pt-5">
                    <AnalysisResult
                      result={{ post: item.post, analysis: item.analysis }}
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
