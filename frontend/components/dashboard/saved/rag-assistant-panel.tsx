"use client";

import { useState } from "react";
import { Card, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ErrorState } from "@/components/ui/error-state";
import { useQueryRagAssistant } from "@/lib/hooks/use-rag";
import type { RagSourceType, SavedAnalysis } from "@/lib/types";

const SOURCE_TYPE_LABEL: Record<RagSourceType, string> = {
  ANALYSIS_RESULT: "投稿分析結果",
  EVALUATION: "投稿評価",
  TREND_REPORT: "トレンドレポート",
  PROPOSAL: "AI企画",
  OTHER: "その他",
};

const SOURCE_TEXT_PREVIEW_LENGTH = 120;

/**
 * 保存済み分析(自動でRAG索引登録される)に対して自然文で質問できるAIアシスタント。
 * バックエンドのRAG質問応答API(Phase16)は実装済みだったがフロントエンドから一度も
 * 呼ばれておらず"死んだ機能"になっていたため、共通点分析パネルと並ぶ差別化機能として
 * 機能化した(戦略監査レポート)。
 */
export function RagAssistantPanel({ items }: { items: SavedAnalysis[] }) {
  const [question, setQuestion] = useState("");
  const mutation = useQueryRagAssistant();

  const handleAsk = () => {
    const trimmed = question.trim();
    if (!trimmed) return;
    mutation.mutate({ question: trimmed });
  };

  if (items.length === 0) {
    return null;
  }

  return (
    <Card>
      <CardHeader
        title="AIアシスタントに質問する"
        description="保存した投稿の分析結果をもとに、AIが自然文の質問に答えます。(例: 「最もCTAが強かった投稿は？」)"
      />

      <div className="flex flex-col gap-2 sm:flex-row">
        <input
          type="text"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              handleAsk();
            }
          }}
          placeholder="保存した投稿について質問する"
          className="min-w-0 flex-1 rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-brand-500 focus:outline-none focus:ring-1 focus:ring-brand-500 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100"
        />
        <Button onClick={handleAsk} disabled={!question.trim()} isLoading={mutation.isPending}>
          質問する
        </Button>
      </div>

      {mutation.isError && (
        <div className="mt-4">
          <ErrorState error={mutation.error} onRetry={handleAsk} />
        </div>
      )}

      {mutation.isSuccess && mutation.data && (
        <div className="mt-5 space-y-4 border-t border-slate-100 pt-5 dark:border-slate-800">
          <p className="whitespace-pre-wrap text-sm text-slate-700 dark:text-slate-300">{mutation.data.answer}</p>

          {mutation.data.sources.length > 0 && (
            <div>
              <p className="text-xs font-semibold text-slate-500">根拠にした保存分析</p>
              <ul className="mt-2 space-y-2">
                {mutation.data.sources.map((source) => (
                  <li
                    key={source.id}
                    className="rounded-lg border border-slate-200 px-3 py-2 text-xs text-slate-500 dark:border-slate-700"
                  >
                    <span className="mb-1 inline-block rounded-full bg-slate-100 px-2 py-0.5 font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                      {SOURCE_TYPE_LABEL[source.sourceType]}
                    </span>
                    <p className="mt-1 line-clamp-2">
                      {source.contentText.length > SOURCE_TEXT_PREVIEW_LENGTH
                        ? `${source.contentText.slice(0, SOURCE_TEXT_PREVIEW_LENGTH)}...`
                        : source.contentText}
                    </p>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </Card>
  );
}
