import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Docker での軽量実行イメージ用に standalone 出力を有効化
  output: "standalone",
  reactStrictMode: true,
  eslint: {
    // Docker ビルド時に lint エラーでビルド全体を止めない（CI 側で別途 lint する想定）
    ignoreDuringBuilds: true,
  },
};

export default nextConfig;
