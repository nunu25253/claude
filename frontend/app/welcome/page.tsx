import type { Metadata } from "next";
import Link from "next/link";
import { Brain, TrendingUp, Search, Trophy } from "lucide-react";

export const metadata: Metadata = {
  title: "ようこそ",
  description:
    "Instagram / TikTok / X の公開投稿をAIが分析し、バズった理由と伸びる投稿の作り方を提案するダッシュボード。",
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

export default function WelcomePage() {
  return (
    <div className="min-h-dvh bg-gradient-to-br from-brand-50 via-white to-slate-50 dark:from-slate-950 dark:via-slate-950 dark:to-slate-900">
      <div className="mx-auto flex max-w-4xl flex-col items-center px-4 py-16 text-center sm:py-24">
        <span className="text-5xl" aria-hidden>
          🚀
        </span>
        <h1 className="mt-4 text-3xl font-bold text-slate-900 dark:text-slate-100 sm:text-4xl">
          SNS AIバズ分析プラットフォーム
        </h1>
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
    </div>
  );
}
