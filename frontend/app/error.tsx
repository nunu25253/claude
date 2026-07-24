"use client";

import { useEffect } from "react";
import Link from "next/link";

/**
 * セグメント内で起きた未捕捉の例外を受け止めるエラーバウンダリ。
 * これが無いとNext.jsの既定表示("Application error: a client-side exception has
 * occurred")がそのままユーザーに見えてしまい、技術者でないユーザーに不安を与える。
 */
export default function ErrorBoundary({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    // 本番では外形監視/エラートラッキングサービスへの送信をここに追加する想定。
    // 現状はコンソールに残すのみ(digestはNext.jsが本番ビルドで生成するエラー識別子)。
    console.error("Unhandled UI error", error);
  }, [error]);

  return (
    <div className="flex min-h-dvh flex-col items-center justify-center gap-3 bg-slate-50 px-4 text-center">
      <p className="text-4xl" aria-hidden>
        ⚠️
      </p>
      <h1 className="text-xl font-bold text-slate-900">一時的な問題が発生しました</h1>
      <p className="max-w-md text-sm text-slate-500">
        ページの表示中に問題が発生しました。お手数ですが再読み込みをお試しください。解決しない場合は時間をおいて再度アクセスしてください。
      </p>
      {error.digest && (
        <p className="text-xs text-slate-500">エラーID: {error.digest}</p>
      )}
      <div className="mt-2 flex gap-3">
        <button type="button" onClick={reset} className="btn-primary">
          再試行
        </button>
        <Link href="/" className="btn-secondary">
          ホームに戻る
        </Link>
      </div>
    </div>
  );
}
