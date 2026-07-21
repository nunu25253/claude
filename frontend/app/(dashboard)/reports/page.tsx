"use client";

import { useState } from "react";
import { useSavedAnalyses } from "@/lib/hooks/use-saved-analyses";
import { useGenerateReport, useReportHistory } from "@/lib/hooks/use-reports";
import { Card, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { QueryState } from "@/components/dashboard/query-state";
import { PlatformBadge } from "@/components/ui/badge";
import { REPORT_FORMAT_OPTIONS } from "@/lib/constants";
import { formatDateTime } from "@/lib/utils";
import type { ReportFormat } from "@/lib/types";

export default function ReportsPage() {
  const savedQuery = useSavedAnalyses();
  const historyQuery = useReportHistory();
  const generateMutation = useGenerateReport();
  // どの投稿×どのフォーマットを生成中かを覚えておき、該当ボタンだけローディング表示する
  const [pendingKey, setPendingKey] = useState<string | null>(null);
  const [lastResult, setLastResult] = useState<{ postId: string; message: string } | null>(null);

  const handleGenerate = async (postId: string, format: ReportFormat) => {
    const key = `${postId}:${format}`;
    setPendingKey(key);
    setLastResult(null);
    try {
      const report = await generateMutation.mutateAsync({ postId, format });
      if (report.downloadUrl) {
        window.open(report.downloadUrl, "_blank", "noopener,noreferrer");
      }
      setLastResult({ postId, message: `${format.toUpperCase()}レポートを生成しました: ${report.fileName}` });
      historyQuery.refetch();
    } catch {
      setLastResult({ postId, message: "レポート生成に失敗しました。時間をおいて再度お試しください。" });
    } finally {
      setPendingKey(null);
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader
          title="レポート生成"
          description="分析済みの投稿からワンクリックでレポートを生成・ダウンロードできます。"
        />
        <QueryState
          isLoading={savedQuery.isLoading}
          isError={savedQuery.isError}
          error={savedQuery.error}
          isEmpty={(savedQuery.data?.length ?? 0) === 0}
          emptyTitle="分析済みの投稿がまだありません"
          emptyDescription="「投稿分析」で分析し、保存した投稿がここに表示されます。"
          onRetry={() => savedQuery.refetch()}
        >
          <div className="space-y-3">
            {savedQuery.data?.map((item) => (
              <div
                key={item.id}
                className="flex flex-col gap-3 rounded-xl border border-slate-100 p-3 sm:flex-row sm:items-center sm:justify-between"
              >
                <div className="min-w-0">
                  <div className="flex items-center gap-2">
                    <PlatformBadge platform={item.post.platform} />
                    <span className="truncate text-sm font-medium text-slate-700">
                      {item.post.caption || "(キャプションなし)"}
                    </span>
                  </div>
                  <p className="mt-0.5 text-xs text-slate-400">
                    保存日 {formatDateTime(item.savedAt)}
                  </p>
                  {lastResult?.postId === item.post.id && (
                    <p className="mt-1 text-xs text-brand-600">{lastResult.message}</p>
                  )}
                </div>
                <div className="flex shrink-0 gap-2">
                  {REPORT_FORMAT_OPTIONS.map((opt) => (
                    <Button
                      key={opt.value}
                      variant="secondary"
                      onClick={() => handleGenerate(item.post.id, opt.value)}
                      isLoading={pendingKey === `${item.post.id}:${opt.value}`}
                    >
                      {opt.label}
                    </Button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </QueryState>
      </Card>

      <Card>
        <CardHeader title="生成履歴" />
        <QueryState
          isLoading={historyQuery.isLoading}
          isError={historyQuery.isError}
          error={historyQuery.error}
          isEmpty={(historyQuery.data?.length ?? 0) === 0}
          emptyTitle="レポートの生成履歴がありません"
          onRetry={() => historyQuery.refetch()}
        >
          <ul className="divide-y divide-slate-100">
            {historyQuery.data?.map((r) => (
              <li key={r.reportId} className="flex items-center justify-between gap-3 py-2.5">
                <div className="min-w-0">
                  <p className="truncate text-sm text-slate-700">{r.postCaption}</p>
                  <p className="text-xs text-slate-400">
                    {r.format.toUpperCase()} ・ {formatDateTime(r.createdAt)}
                  </p>
                </div>
                {r.downloadUrl && (
                  <a
                    href={r.downloadUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="shrink-0 text-sm font-medium text-brand-600 hover:underline"
                  >
                    ダウンロード
                  </a>
                )}
              </li>
            ))}
          </ul>
        </QueryState>
      </Card>
    </div>
  );
}
