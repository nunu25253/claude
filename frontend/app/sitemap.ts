import type { MetadataRoute } from "next";

const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";

/**
 * 索引化に値するのは公開ランディングページ(/welcome)のみ。
 * ダッシュボード配下・認証ページは認証必須かつユーザー固有の内容のため対象外(robots.tsと対応)。
 */
export default function sitemap(): MetadataRoute.Sitemap {
  return [
    {
      url: `${SITE_URL}/welcome`,
      lastModified: new Date(),
      changeFrequency: "monthly",
      priority: 1,
    },
  ];
}
