import type { MetadataRoute } from "next";

/**
 * PWAインストール可能にするためのWebアプリマニフェスト。/manifest.webmanifest として自動配信される。
 * アイコンは既存のapp/icon.svg(favicon用ブランドマーク)を流用し、新規アセットは追加していない。
 */
export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "Buzzly — SNS AIバズ分析",
    short_name: "Buzzly",
    description: "Instagram / TikTok / X の公開投稿をAIが分析し、バズった理由と伸びる投稿の作り方を提案するダッシュボード。",
    start_url: "/",
    display: "standalone",
    background_color: "#ffffff",
    theme_color: "#4a63f5",
    lang: "ja",
    icons: [
      {
        src: "/icon.svg",
        sizes: "any",
        type: "image/svg+xml",
        purpose: "any",
      },
    ],
  };
}
