import { NextRequest, NextResponse } from "next/server";

// バックエンド(AuthCookieNames.ACCESS_TOKEN)が発行するHttpOnly Cookieの名前と同じ値。
// HttpOnlyはブラウザのJSからの読み取りを禁じるだけで、サーバーサイドのmiddlewareが
// リクエストヘッダー経由でCookieの有無を見ることは問題無い(実際の署名検証はバックエンド側で行う。
// ここでは「Cookieが存在するか」だけを見た簡易ガード)。
const ACCESS_TOKEN_COOKIE = "access_token";

// ログイン不要でアクセスできるパス
const PUBLIC_PATHS = ["/login", "/register", "/forgot-password", "/reset-password", "/verify-email", "/welcome"];
// ログイン済みならダッシュボードへ戻すパス(/verify-email は登録直後の
// ログイン済みユーザーもアクセスするため対象外とする)
const AUTH_ONLY_PATHS = ["/login", "/register", "/forgot-password", "/reset-password"];

/**
 * ダッシュボード配下は未ログインの場合 /login にリダイレクトする簡易ガード。
 * JWTの中身の検証はバックエンド側に委ね、ここでは「Cookieの有無」のみをチェックする。
 */
export function middleware(request: NextRequest) {
  const token = request.cookies.get(ACCESS_TOKEN_COOKIE)?.value;
  const { pathname } = request.nextUrl;

  const isPublicPath = PUBLIC_PATHS.some((p) => pathname.startsWith(p));
  const isAuthOnlyPath = AUTH_ONLY_PATHS.some((p) => pathname.startsWith(p));

  if (!token && !isPublicPath) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("next", pathname);
    return NextResponse.redirect(loginUrl);
  }

  if (token && isAuthOnlyPath) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * 静的ファイル・APIルート・OGP/favicon等のメタデータルートを除いた全パスに適用。
     * これらを除外しないとSNSクローラーやブラウザタブアイコン取得が/loginへリダイレクトされ、
     * OGPプレビューやfaviconが機能しなくなる。
     */
    "/((?!_next/static|_next/image|favicon.ico|icon.svg|apple-icon|opengraph-image|twitter-image|robots.txt|sitemap.xml|api).*)",
  ],
};
