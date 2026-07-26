import type { Metadata, Viewport } from "next";
import { headers } from "next/headers";
import "./globals.css";
import { Providers } from "./providers";

const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";
const SITE_TITLE = "Buzzly";
const SITE_DESCRIPTION =
  "Instagram / TikTok / X の公開投稿をAIが分析し、バズった理由と伸びる投稿の作り方を提案するダッシュボード。";

export const metadata: Metadata = {
  metadataBase: new URL(SITE_URL),
  title: {
    default: SITE_TITLE,
    template: `%s | ${SITE_TITLE}`,
  },
  description: SITE_DESCRIPTION,
  openGraph: {
    title: SITE_TITLE,
    description: SITE_DESCRIPTION,
    url: SITE_URL,
    siteName: SITE_TITLE,
    locale: "ja_JP",
    type: "website",
  },
  twitter: {
    card: "summary_large_image",
    title: SITE_TITLE,
    description: SITE_DESCRIPTION,
  },
  appleWebApp: {
    capable: true,
    statusBarStyle: "default",
    title: SITE_TITLE,
  },
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#4a63f5",
};

// ダークモード設定をハイドレーション前に適用し、切り替え時のちらつき(FOUC)を防ぐ。
const THEME_INIT_SCRIPT = `
(function () {
  try {
    var stored = localStorage.getItem("sns_buzz_theme");
    var isDark = stored ? stored === "dark" : window.matchMedia("(prefers-color-scheme: dark)").matches;
    if (isDark) document.documentElement.classList.add("dark");
  } catch (e) {}
})();
`;

export default async function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  // middleware.tsがリクエスト毎に生成しx-nonceヘッダーで転送したnonce。
  // CSPのscript-srcをnonceベースに厳格化した(unsafe-inline廃止)ため、
  // インラインスクリプトにはこのnonceを付与する必要がある。
  const nonce = (await headers()).get("x-nonce") ?? undefined;

  return (
    <html lang="ja" suppressHydrationWarning>
      <head>
        <script nonce={nonce} dangerouslySetInnerHTML={{ __html: THEME_INIT_SCRIPT }} />
      </head>
      <body className="antialiased">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
