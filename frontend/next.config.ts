import type { NextConfig } from "next";

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
  // Content-Security-Policyはリクエスト毎のnonceが必要なため、静的なこのheaders()では
  // 表現できず middleware.ts で動的に設定する。
  async headers() {
    return [
      {
        source: "/:path*",
        headers: [
          { key: "X-Frame-Options", value: "DENY" },
          { key: "X-Content-Type-Options", value: "nosniff" },
          { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
          { key: "Strict-Transport-Security", value: "max-age=63072000; includeSubDomains" },
        ],
      },
    ];
  },
};

export default nextConfig;
