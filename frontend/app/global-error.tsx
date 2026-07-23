"use client";

import { useEffect } from "react";

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
        <div style={{ display: "flex", minHeight: "100dvh", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: "12px", padding: "16px", textAlign: "center", fontFamily: "sans-serif" }}>
          <p style={{ fontSize: "2.25rem" }} aria-hidden>⚠️</p>
          <h1 style={{ fontSize: "1.25rem", fontWeight: 700 }}>アプリケーションの読み込みに失敗しました</h1>
          <p style={{ maxWidth: "28rem", fontSize: "0.875rem", color: "#64748b" }}>
            時間をおいて再度アクセスしてください。問題が続く場合はサポートまでご連絡ください。
          </p>
          {error.digest && (
            <p style={{ fontSize: "0.75rem", color: "#94a3b8" }}>エラーID: {error.digest}</p>
          )}
          <button
            type="button"
            onClick={reset}
            style={{ marginTop: "8px", borderRadius: "8px", background: "#3947d6", color: "#fff", padding: "8px 20px", fontWeight: 600, fontSize: "0.875rem", border: "none", cursor: "pointer" }}
          >
            再試行
          </button>
        </div>
      </body>
    </html>
  );
}
