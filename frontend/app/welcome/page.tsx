import type { Metadata } from "next";
import Link from "next/link";
import { headers } from "next/headers";
import { Brain, TrendingUp, Search, Trophy, ShieldCheck, Lock, FileCheck } from "lucide-react";
import { BrandMark } from "@/components/layout/brand-mark";
import { SiteFooter } from "@/components/layout/site-footer";

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

// billing/page.tsx の PLAN_COMPARISON と一致させている(値を変える場合は両方を更新すること)。
const PRICING_PREVIEW = {
  free: { priceLabel: "無料", dailyAnalysisLimit: 50 },
  pro: { priceLabel: "¥4,980 / 月", dailyAnalysisLimit: 500 },
};

// 実装済みで裏付けのある事実のみを掲載する(実績数値・レビュー等、裏付けの無い訴求は行わない)。
const TRUST_SIGNALS = [
  { icon: Lock, text: "通信は全てHTTPSで暗号化。認証情報はHTTP Only Cookieで保護" },
  { icon: ShieldCheck, text: "登録フォームはCloudflare CAPTCHAでボット対策済み" },
  { icon: FileCheck, text: "個人データのエクスポート・アカウント削除をいつでもセルフサービスで実行可能" },
];

const FAQ = [
  {
    q: "分析できるのは自分のSNSアカウントだけですか?",
    a: "いいえ。公開されている投稿であれば、自分のアカウント・他アカウントのどちらの投稿URLでも分析できます。まずは気になるバズ投稿を分析してみるのがおすすめです。",
  },
  {
    q: "対応しているSNSプラットフォームは?",
    a: "Instagram / TikTok / X(旧Twitter)の公開投稿に対応しています。",
  },
  {
    q: "無料プランでどこまで使えますか?",
    a: "1日50回まで投稿分析AIを無料でご利用いただけます。トレンド・ランキング・保存済み分析などの閲覧機能に制限はありません。",
  },
  {
    q: "料金プランはいつでも変更できますか?",
    a: "はい。設定画面からいつでもPROプランへのアップグレード・解約が可能です。解約は即時反映され、日割り請求は発生しません。",
  },
];

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

        <div className="mt-16 flex w-full flex-col items-center gap-3 sm:flex-row sm:justify-center sm:gap-8">
          {TRUST_SIGNALS.map((signal) => (
            <div key={signal.text} className="flex max-w-xs items-start gap-2 text-left">
              <signal.icon className="mt-0.5 h-4 w-4 shrink-0 text-brand-600 dark:text-brand-400" aria-hidden />
              <p className="text-xs text-slate-500 dark:text-slate-400">{signal.text}</p>
            </div>
          ))}
        </div>

        <div className="mt-20 w-full">
          <h2 className="text-xl font-bold text-slate-900 dark:text-slate-100">料金プラン</h2>
          <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
            <div className="rounded-2xl border border-slate-200 bg-white p-6 text-left shadow-card dark:border-slate-800 dark:bg-slate-900">
              <p className="text-sm font-semibold text-slate-500">FREE</p>
              <p className="mt-1 text-2xl font-bold text-slate-900 dark:text-slate-100">
                {PRICING_PREVIEW.free.priceLabel}
              </p>
              <p className="mt-2 text-xs text-slate-500">1日あたり{PRICING_PREVIEW.free.dailyAnalysisLimit}回まで投稿分析AIを利用可能</p>
            </div>
            <div className="rounded-2xl border-2 border-brand-600 bg-white p-6 text-left shadow-card dark:bg-slate-900">
              <p className="text-sm font-semibold text-brand-600">PRO</p>
              <p className="mt-1 text-2xl font-bold text-slate-900 dark:text-slate-100">
                {PRICING_PREVIEW.pro.priceLabel}
              </p>
              <p className="mt-2 text-xs text-slate-500">1日あたり{PRICING_PREVIEW.pro.dailyAnalysisLimit}回まで投稿分析AIを利用可能</p>
            </div>
          </div>
        </div>

        <div className="mt-20 w-full text-left">
          <h2 className="text-center text-xl font-bold text-slate-900 dark:text-slate-100">よくある質問</h2>
          <div className="mt-6 space-y-3">
            {FAQ.map((item) => (
              <details
                key={item.q}
                className="group rounded-xl border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
              >
                <summary className="cursor-pointer list-none text-sm font-semibold text-slate-900 dark:text-slate-100">
                  {item.q}
                </summary>
                <p className="mt-2 text-sm text-slate-600 dark:text-slate-300">{item.a}</p>
              </details>
            ))}
          </div>
        </div>
      </div>

      <SiteFooter />
    </main>
  );
}
