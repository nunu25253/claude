"use client";

import { use } from "react";
import Link from "next/link";
import { BrandMark } from "@/components/layout/brand-mark";
import { SiteFooter } from "@/components/layout/site-footer";
import { QueryState } from "@/components/dashboard/query-state";
import { AnalysisResult } from "@/components/dashboard/analysis/analysis-result";
import { useSharedAnalysis } from "@/lib/hooks/use-shared";

/**
 * 外部共有リンク経由の分析結果閲覧ページ。未ログインでもアクセス可能
 * (シニアレビュー: 「外部クライアント向けの閲覧専用共有リンクが無い」への対応)。
 * ログイン画面へのリダイレクトを防ぐため middleware.ts の PUBLIC_PATHS に /shared を登録済み。
 */
export default function SharedAnalysisPage({ params }: { params: Promise<{ token: string }> }) {
  const { token } = use(params);
  const sharedQuery = useSharedAnalysis(token);
  const item = sharedQuery.data;

  return (
    <div className="flex min-h-dvh flex-col bg-white dark:bg-slate-950">
      <header className="border-b border-slate-200 dark:border-slate-800">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-4">
          <Link href="/welcome" className="flex items-center gap-2 text-slate-900 dark:text-slate-100">
            <BrandMark className="h-6 w-6 text-brand-600" />
            <span className="text-sm font-semibold">Buzzly</span>
          </Link>
          <span className="text-xs text-slate-500 dark:text-slate-400">共有された分析結果(閲覧専用)</span>
        </div>
      </header>

      <main className="mx-auto w-full max-w-3xl flex-1 px-4 py-10">
        <QueryState
          isLoading={sharedQuery.isLoading}
          isError={sharedQuery.isError}
          error={sharedQuery.error}
          isEmpty={false}
          onRetry={() => sharedQuery.refetch()}
        >
          {item && item.analysis && item.buzzScore && (
            <AnalysisResult
              result={{
                post: item.post,
                analysis: item.analysis,
                buzzScore: item.buzzScore,
                similarPosts: [],
              }}
              hideSaveAction
            />
          )}
        </QueryState>
      </main>

      <SiteFooter />
    </div>
  );
}
