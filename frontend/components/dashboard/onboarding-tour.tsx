"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { Brain, TrendingUp, Trophy, Bookmark, type LucideIcon } from "lucide-react";
import { useAuth } from "@/lib/auth/auth-context";

const STORAGE_KEY_PREFIX = "sns_buzz_onboarding_dismissed_";

const FOCUSABLE_SELECTOR = 'a[href], button:not([disabled]), input, select, textarea, [tabindex]:not([tabindex="-1"])';

interface OnboardingStep {
  href: string;
  icon: LucideIcon;
  title: string;
  description: string;
}

// サイドバー(nav-items.ts)・トップページ(app/welcome/page.tsx)と同じLucideアイコンを使い、
// 絵文字とのブランド不統一を解消する。
// 投稿分析は「まず1件試す」体験用の主要CTA(下部ボタン)に格上げしたため、このリストには含めない
// (戦略監査レポート4章: 説明を読むツアーから実操作を促すツアーへの改修)。
const STEPS: OnboardingStep[] = [
  {
    href: "/trend",
    icon: TrendingUp,
    title: "トレンド",
    description: "ジャンル別に今伸びている投稿の傾向をチェックできます。",
  },
  {
    href: "/rankings",
    icon: Trophy,
    title: "ランキング",
    description: "分析済み投稿をBuzzScoreの高い順に並べて比較できます。",
  },
  {
    href: "/saved",
    icon: Bookmark,
    title: "保存済み分析",
    description: "気になる分析結果を保存し、あとから見返したりCSVで書き出せます。",
  },
];

export function OnboardingTour() {
  const { user } = useAuth();
  const [visible, setVisible] = useState(false);
  const panelRef = useRef<HTMLDivElement>(null);
  const previouslyFocusedElementRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!user) return;
    const dismissed = window.localStorage.getItem(`${STORAGE_KEY_PREFIX}${user.id}`);
    if (!dismissed) {
      setVisible(true);
    }
  }, [user]);

  const dismiss = useCallback(() => {
    if (user) {
      window.localStorage.setItem(`${STORAGE_KEY_PREFIX}${user.id}`, "1");
    }
    setVisible(false);
  }, [user]);

  // モーダル表示時にフォーカスを内部の最初の要素へ移し、閉じたら開く直前にフォーカスされていた
  // 要素へ戻す(キーボード/スクリーンリーダー利用者がモーダルの外へフォーカスを見失わないため)。
  useEffect(() => {
    if (!visible) return;
    previouslyFocusedElementRef.current = document.activeElement as HTMLElement | null;
    panelRef.current?.querySelector<HTMLElement>(FOCUSABLE_SELECTOR)?.focus();

    return () => {
      previouslyFocusedElementRef.current?.focus();
    };
  }, [visible]);

  // Tab/Shift+Tabをモーダル内の要素間のみで循環させ(フォーカストラップ)、
  // Escapeキーで閉じられるようにする。
  const handleKeyDown = useCallback(
    (event: React.KeyboardEvent<HTMLDivElement>) => {
      if (event.key === "Escape") {
        event.preventDefault();
        dismiss();
        return;
      }
      if (event.key !== "Tab" || !panelRef.current) return;

      const focusable = Array.from(panelRef.current.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR));
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      if (!first || !last) return;

      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault();
        first.focus();
      }
    },
    [dismiss],
  );

  if (!visible) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 px-4 py-8"
      role="dialog"
      aria-modal="true"
      aria-labelledby="onboarding-title"
      onKeyDown={handleKeyDown}
    >
      <div ref={panelRef} className="w-full max-w-lg rounded-2xl bg-white p-6 shadow-xl dark:bg-slate-900">
        <h2 id="onboarding-title" className="text-lg font-bold text-slate-900 dark:text-slate-100">
          ようこそ、Buzzlyへ
        </h2>
        <p className="mt-1 text-sm text-slate-500">
          メールアドレスの確認前でも、投稿URLを1件だけ無料で分析できます。まずは気になる投稿を1つ試してみましょう。
        </p>

        <ul className="mt-5 space-y-3">
          {STEPS.map((step) => (
            <li key={step.href}>
              <Link
                href={step.href}
                onClick={dismiss}
                className="flex items-start gap-3 rounded-xl border border-slate-200 p-3 transition hover:border-brand-300 hover:bg-brand-50 dark:border-slate-700 dark:hover:border-brand-700 dark:hover:bg-brand-950/40"
              >
                <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-brand-50 text-brand-600 dark:bg-brand-950 dark:text-brand-300">
                  <step.icon className="h-4 w-4" aria-hidden />
                </span>
                <span>
                  <span className="block text-sm font-semibold text-slate-900 dark:text-slate-100">{step.title}</span>
                  <span className="block text-xs text-slate-500">{step.description}</span>
                </span>
              </Link>
            </li>
          ))}
        </ul>

        <Link href="/posts/analyze" onClick={dismiss} className="btn-primary mt-6 flex w-full items-center justify-center gap-2">
          <Brain className="h-4 w-4" aria-hidden />
          投稿URLを分析してみる
        </Link>
      </div>
    </div>
  );
}
