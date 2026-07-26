import Link from "next/link";
import { BrandMark } from "@/components/layout/brand-mark";
import { SiteFooter } from "@/components/layout/site-footer";

/**
 * 利用規約・プライバシーポリシー・特定商取引法ページ共通のシェル。
 * ログイン不要でアクセスできる必要があるため middleware.ts の PUBLIC_PATHS に
 * 各ページのパスを追加すること。
 */
export function LegalPageShell({
  title,
  updatedAt,
  children,
}: {
  title: string;
  updatedAt: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex min-h-dvh flex-col bg-white dark:bg-slate-950">
      <header className="border-b border-slate-200 dark:border-slate-800">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-4 py-4">
          <Link href="/welcome" className="flex items-center gap-2 text-slate-900 dark:text-slate-100">
            <BrandMark className="h-6 w-6 text-brand-600" />
            <span className="text-sm font-semibold">Buzzly</span>
          </Link>
          <Link href="/welcome" className="text-xs text-slate-500 hover:text-brand-600 dark:text-slate-400">
            トップへ戻る
          </Link>
        </div>
      </header>

      <main className="mx-auto w-full max-w-3xl flex-1 px-4 py-12">
        <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">{title}</h1>
        <p className="mt-1 text-xs text-slate-500 dark:text-slate-400">最終改定日: {updatedAt}</p>
        <div className="legal-content mt-8 text-sm leading-7 text-slate-700 dark:text-slate-300">{children}</div>
      </main>

      <SiteFooter />
    </div>
  );
}
