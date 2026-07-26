import type { MetadataRoute } from "next";

const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL ?? "http://localhost:3000";

/**
 * ダッシュボード配下(/、/posts等)は認証必須でログインしないと閲覧できず、
 * 検索エンジンにとって索引化する価値が無い(クロールしても/loginへのリダイレクトしか返らない)ため、
 * 公開ランディングページ(/welcome)のみクロールを許可する。
 */
export default function robots(): MetadataRoute.Robots {
  return {
    rules: {
      userAgent: "*",
      allow: "/welcome",
      disallow: "/",
    },
    sitemap: `${SITE_URL}/sitemap.xml`,
  };
}
