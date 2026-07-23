"use client";

import { useState } from "react";
import { useTeamSavedAnalyses } from "@/lib/hooks/use-organizations";
import { QueryState } from "@/components/dashboard/query-state";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { PlatformBadge } from "@/components/ui/badge";
import { AnalysisResult } from "@/components/dashboard/analysis/analysis-result";
import { formatDateTime } from "@/lib/utils";

export function TeamSavedAnalysesPanel({ organizationId }: { organizationId: string }) {
  const teamSavedQuery = useTeamSavedAnalyses(organizationId);
  const [expandedId, setExpandedId] = useState<string | null>(null);

  return (
    <QueryState
      isLoading={teamSavedQuery.isLoading}
      isError={teamSavedQuery.isError}
      error={teamSavedQuery.error}
      isEmpty={(teamSavedQuery.data?.length ?? 0) === 0}
      emptyTitle="チームの保存済み分析がありません"
      emptyDescription="メンバーが「保存済み分析」に投稿を保存すると、ここに一覧表示されます。"
      onRetry={() => teamSavedQuery.refetch()}
    >
      <div className="space-y-3">
        {teamSavedQuery.data?.map(({ savedAnalysis, savedByDisplayName }) => {
          const isExpanded = expandedId === savedAnalysis.id;
          return (
            <Card key={savedAnalysis.id}>
              <div className="flex items-center gap-4">
                <div className="min-w-0 flex-1">
                  <div className="flex items-center gap-2">
                    <PlatformBadge platform={savedAnalysis.post.platform} />
                    <span className="truncate text-sm font-medium text-slate-700">
                      {savedAnalysis.post.caption || "(キャプションなし)"}
                    </span>
                  </div>
                  <p className="mt-0.5 text-xs text-slate-400">
                    保存者 {savedByDisplayName} ・ 保存日 {formatDateTime(savedAnalysis.createdAt)}
                  </p>
                  {savedAnalysis.note && (
                    <p className="mt-1 text-xs text-slate-500">メモ: {savedAnalysis.note}</p>
                  )}
                </div>
                {savedAnalysis.analysis && savedAnalysis.buzzScore && (
                  <Button
                    variant="secondary"
                    onClick={() => setExpandedId(isExpanded ? null : savedAnalysis.id)}
                  >
                    {isExpanded ? "閉じる" : "詳細を見る"}
                  </Button>
                )}
              </div>

              {isExpanded && savedAnalysis.analysis && savedAnalysis.buzzScore && (
                <div className="mt-5 border-t border-slate-100 pt-5">
                  <AnalysisResult
                    result={{
                      post: savedAnalysis.post,
                      analysis: savedAnalysis.analysis,
                      buzzScore: savedAnalysis.buzzScore,
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
  );
}
