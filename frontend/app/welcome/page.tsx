import type { Metadata } from "next";
import Link from "next/link";
import { headers } from "next/headers";
import { Brain, TrendingUp, Search, Trophy } from "lucide-react";
import { BrandMark } from "@/components/layout/brand-mark";

export const metadata: Metadata = {
  title: "ようこそ",
  description:
    "Instagram / TikTok / X の公開投稿をAIが分析し、バズった理由と伸びる投稿の作り方を提案するダッシュボード。",
};

// 検索エンジン向けの構造化データ(schema.org SoftwareApplication)。
// 価格・評価等、事実として裏付けの無い項目(offers/aggregateRating等)は含めない。
const STRUCTURED_DATA = {
  "@context": "https://schema.org",
  "@type": "SoftwareApplication",
  name: "Buzzly",
  description:
    "Instagram / TikTok / X の公開投稿をAIが分析し、バズった理由と伸びる投稿の作り方を提案するダッシュボード。",
  applicationCategory: "BusinessApplication",
  operatingSystem: "Web",
};

const FEATURES = [
  {
    icon: Brain,
    title: "投稿分析",
    description: "投稿URLを入力するだけで、AIがバズった理由と改善案を自動で提案します。",
  },
  {
    icon: TrendingUp,
    title: "トレンド",
    description: "ジャンル別に今伸びている投稿の傾向をいち早くキャッチできます。",
  },
  {
    icon: Search,
    title: "競合分析",
    description: "競合アカウントの投稿傾向・エンゲージメントを可視化して比較できます。",
  },
  {
    icon: Trophy,
    title: "ランキング",
    description: "分析済み投稿をBuzzScoreの高い順に並べて確認できます。",
  },
];

export default async function WelcomePage() {
  // middleware.tsがリクエスト毎に生成しx-nonceヘッダーで転送したnonce。
  // CSPのscript-srcをnonceベースに厳格化しているため、JSON-LDのscriptタグにも必要。
  const nonce = (await headers()).get("x-nonce") ?? undefined;

  return (
    <main className="min-h-dvh bg-gradient-to-br from-brand-50 via-white to-slate-50 dark:from-slate-950 dark:via-slate-950 dark:to-slate-900">
      <script
        type="application/ld+json"
        nonce={nonce}
        dangerouslySetInnerHTML={{ __html: JSON.stringify(STRUCTURED_DATA) }}
      />
      <div className="mx-auto flex max-w-4xl flex-col items-center px-4 py-16 text-center sm:py-24">
        <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-brand-600 shadow-card">
          <BrandMark className="h-9 w-9 text-white" />
        </div>
        <h1 className="mt-4 text-3xl font-bold text-slate-900 dark:text-slate-100 sm:text-4xl">
          Buzzly
        </h1>
        <p className="mt-1 text-sm font-medium text-brand-600 dark:text-brand-400">
          SNS AIバズ分析プラットフォーム
        </p>
        <p className="mt-4 max-w-2xl text-base text-slate-600 dark:text-slate-300">
          Instagram / TikTok / X の公開投稿をAIが分析し、バズった理由と伸びる投稿の作り方を提案します。
        </p>

        <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
          <Link href="/register" className="btn-primary px-6 py-2.5 text-base">
            無料ではじめる
          </Link>
          <Link href="/login" className="btn-secondary px-6 py-2.5 text-base">
            ログイン
          </Link>
        </div>

        <div className="mt-16 grid w-full grid-cols-1 gap-4 sm:grid-cols-2">
          {FEATURES.map((feature) => (
            <div
              key={feature.title}
              className="flex items-start gap-3 rounded-2xl border border-slate-200 bg-white p-5 text-left shadow-card dark:border-slate-800 dark:bg-slate-900"
            >
              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-brand-600 dark:bg-brand-950 dark:text-brand-300">
                <feature.icon className="h-5 w-5" aria-hidden />
              </div>
              <div>
                <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">{feature.title}</p>
                <p className="mt-0.5 text-xs text-slate-500">{feature.description}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </main>
  );
}
