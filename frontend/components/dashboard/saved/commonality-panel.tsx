"use client";

import { useState } from "react";
import { Card, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ErrorState } from "@/components/ui/error-state";
import { PlatformBadge } from "@/components/ui/badge";
import { useAnalyzeCommonality } from "@/lib/hooks/use-commonality";
import type { ContentFormat, SavedAnalysis } from "@/lib/types";

const CONTENT_FORMAT_LABEL: Record<ContentFormat, string> = {
  SHORT_VIDEO: "短尺動画",
  LONG_VIDEO: "長尺動画",
  SINGLE_IMAGE: "単一画像",
  MULTI_IMAGE_CAROUSEL: "複数画像(カルーセル)",
  TEXT_ONLY: "テキストのみ",
};

/**
 * 保存済み分析から選んだ投稿群の「共通点」(勝ちパターン)をAIが抽出する。
 * バックエンドの共通点分析API(Phase8)は実装済みだったがフロントエンドから一度も
 * 呼ばれておらず"死んだ機能"になっていたため、このパネルとして機能化した
 * (戦略監査レポート: 差別化機能の最有力候補)。
 */
export function CommonalityPanel({ items }: { items: SavedAnalysis[] }) {
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const mutation = useAnalyzeCommonality();

  const toggle = (postId: string) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(postId)) {
        next.delete(postId);
      } else {
        next.add(postId);
      }
      return next;
    });
  };

  const canAnalyze = selectedIds.size >= 2;

  const handleAnalyze = () => {
    if (!canAnalyze) return;
    mutation.mutate({ postIds: Array.from(selectedIds) });
  };

  if (items.length === 0) {
    return null;
  }

  return (
    <Card>
      <CardHeader
        title="共通点分析(勝ちパターン抽出)"
        description="保存した投稿から2件以上選ぶと、共通するハッシュタグ・投稿時間・構成パターンをAIがまとめて抽出します。"
      />

      <ul className="max-h-64 space-y-1.5 overflow-y-auto rounded-lg border border-slate-200 p-2 dark:border-slate-700">
        {items.map((item) => (
          <li key={item.post.id}>
            <label className="flex cursor-pointer items-center gap-2.5 rounded-md px-2 py-1.5 text-sm hover:bg-slate-50 dark:hover:bg-slate-800">
              <input
                type="checkbox"
                checked={selectedIds.has(item.post.id)}
                onChange={() => toggle(item.post.id)}
                className="h-4 w-4 rounded border-slate-300 text-brand-600 focus:ring-brand-500"
              />
              <PlatformBadge platform={item.post.platform} />
              <span className="min-w-0 flex-1 truncate text-slate-700 dark:text-slate-300">
                {item.post.caption || "(キャプションなし)"}
              </span>
            </label>
          </li>
        ))}
      </ul>

      <div className="mt-4 flex items-center gap-3">
        <Button onClick={handleAnalyze} disabled={!canAnalyze} isLoading={mutation.isPending}>
          共通点を分析する
        </Button>
        <span className="text-xs text-slate-500">
          {selectedIds.size}件選択中{!canAnalyze && "(2件以上選んでください)"}
        </span>
      </div>

      {mutation.isError && (
        <div className="mt-4">
          <ErrorState error={mutation.error} onRetry={handleAnalyze} />
        </div>
      )}

      {mutation.isSuccess && mutation.data && (
        <div className="mt-5 space-y-4 border-t border-slate-100 pt-5 dark:border-slate-800">
          <div className="flex flex-wrap gap-2 text-xs text-slate-500">
            <span>対象投稿 {mutation.data.totalPostCount}件</span>
            <span>AIサンプル {mutation.data.aiSampleSize}件</span>
          </div>

          {mutation.data.commonHashtags.length > 0 && (
            <div>
              <p className="text-xs font-semibold text-slate-500">共通ハッシュタグ</p>
              <div className="mt-1 flex flex-wrap gap-1.5">
                {mutation.data.commonHashtags.map((tag) => (
                  <span
                    key={tag}
                    className="rounded-full bg-brand-50 px-2.5 py-0.5 text-xs text-brand-700 dark:bg-brand-950 dark:text-brand-300"
                  >
                    #{tag}
                  </span>
                ))}
              </div>
            </div>
          )}

          <dl className="grid grid-cols-1 gap-x-6 gap-y-3 text-sm sm:grid-cols-2">
            {mutation.data.commonVideoDurationSeconds != null && (
              <div>
                <dt className="text-xs font-semibold text-slate-500">共通の動画時間</dt>
                <dd className="mt-0.5 text-slate-700 dark:text-slate-300">
                  約{mutation.data.commonVideoDurationSeconds}秒
                </dd>
              </div>
            )}
            {mutation.data.commonPostingHour != null && (
              <div>
                <dt className="text-xs font-semibold text-slate-500">共通の投稿時間帯</dt>
                <dd className="mt-0.5 text-slate-700 dark:text-slate-300">
                  {mutation.data.commonPostingHour}時台
                </dd>
              </div>
            )}
            {mutation.data.commonContentFormat && (
              <div>
                <dt className="text-xs font-semibold text-slate-500">共通のコンテンツ形式</dt>
                <dd className="mt-0.5 text-slate-700 dark:text-slate-300">
                  {CONTENT_FORMAT_LABEL[mutation.data.commonContentFormat]}
                </dd>
              </div>
            )}
          </dl>

          {[
            { label: "共通タイトルパターン", value: mutation.data.commonTitlePattern },
            { label: "共通フックパターン", value: mutation.data.commonHookPattern },
            { label: "共通CTAパターン", value: mutation.data.commonCtaPattern },
            { label: "共通構成パターン", value: mutation.data.commonStructurePattern },
            { label: "共通ターゲットパターン", value: mutation.data.commonTargetPattern },
          ]
            .filter((row) => row.value)
            .map((row) => (
              <div key={row.label}>
                <p className="text-xs font-semibold text-slate-500">{row.label}</p>
                <p className="mt-0.5 text-sm text-slate-700 dark:text-slate-300">{row.value}</p>
              </div>
            ))}
        </div>
      )}
    </Card>
  );
}
