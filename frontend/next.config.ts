import type { NextConfig } from "next";

// ブラウザから直接叩くバックエンドのオリジンをCSPのconnect-srcに許可する必要があるため、
// APIベースURLからオリジン部分だけを抜き出す(パス部分/api/v1は不要)。
function apiOrigin(): string {
  const base = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";
  try {
    return new URL(base).origin;
  } catch {
    return "";
  }
}

const nextConfig: NextConfig = {
  // Docker での軽量実行イメージ用に standalone 出力を有効化
  output: "standalone",
  reactStrictMode: true,
  eslint: {
    // Docker ビルド時に lint エラーでビルド全体を止めない（CI 側で別途 lint する想定）
    ignoreDuringBuilds: true,
  },
  // セキュリティヘッダーが一切設定されておらず、クリックジャッキングやMIMEスニッフィング、
  // 混在コンテンツへの多層防御が無かったための追加。
  async headers() {
    return [
      {
        source: "/:path*",
        headers: [
          { key: "X-Frame-Options", value: "DENY" },
          { key: "X-Content-Type-Options", value: "nosniff" },
          { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
          { key: "Strict-Transport-Security", value: "max-age=63072000; includeSubDomains" },
          {
            key: "Content-Security-Policy",
            value: [
              "default-src 'self'",
              // SNS投稿のサムネイル/アバターは外部ドメインの画像を表示するためhttps全般を許可する
              "img-src 'self' https: data:",
              // Next.jsのハイドレーション用インラインスクリプトのためunsafe-inlineを許可
              // (nonceベースの厳格化は別途対応)。開発時のみReact Fast Refreshがeval()を使うため
              // unsafe-evalも許可する(本番ビルドでは不要かつ許可しない)。
              `script-src 'self' 'unsafe-inline'${process.env.NODE_ENV === "production" ? "" : " 'unsafe-eval'"}`,
              "style-src 'self' 'unsafe-inline'",
              `connect-src 'self' ${apiOrigin()}`.trim(),
            ].join("; "),
          },
        ],
      },
    ];
  },
};

export default nextConfig;
