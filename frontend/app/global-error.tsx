"use client";

import { useEffect } from "react";
// ルートレイアウト(app/layout.tsx)を経由しないため、Tailwindの生成物を
// このファイル自身でインポートする必要がある(App Routerではグローバルレイアウト外の
// コンポーネントからのグローバルCSS importも許可されている)。CSPのstyle-srcから
// unsafe-inlineを排除したため、インラインstyle属性ではなくクラス名で装飾する。
import "./globals.css";

/**
 * ルートレイアウト自体で例外が起きた場合のフォールバック(Providers等の初期化失敗など)。
 * app/error.tsx ではカバーできないため別途必要。Next.jsの仕様上、独自のhtml/bodyを描画する。
 */
export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("Unhandled root-level error", error);
  }, [error]);

  return (
    <html lang="ja">
      <body>
        <div className="flex min-h-dvh flex-col items-center justify-center gap-3 p-4 text-center font-sans">
          <p className="text-4xl" aria-hidden>⚠️</p>
          <h1 className="text-xl font-bold">アプリケーションの読み込みに失敗しました</h1>
          <p className="max-w-md text-sm text-slate-500">
            時間をおいて再度アクセスしてください。問題が続く場合はサポートまでご連絡ください。
          </p>
          {error.digest && (
            <p className="text-xs text-slate-500">エラーID: {error.digest}</p>
          )}
          <button
            type="button"
            onClick={reset}
            className="mt-2 rounded-lg border-none bg-brand-600 px-5 py-2 text-sm font-semibold text-white transition hover:bg-brand-700"
          >
            再試行
          </button>
        </div>
      </body>
    </html>
  );
}
