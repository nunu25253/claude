import { NextRequest, NextResponse } from "next/server";

// token.ts の DEFAULT_TOKEN_KEY と同じ値（env未設定時のフォールバック）
const DEFAULT_TOKEN_KEY = "sns_buzz_auth_token";

const PUBLIC_PATHS = ["/login", "/register", "/forgot-password", "/reset-password"];

/**
 * ダッシュボード配下は未ログインの場合 /login にリダイレクトする簡易ガード。
 * JWTの中身の検証はバックエンド側に委ね、ここでは「Cookieの有無」のみをチェックする。
 */
export function middleware(request: NextRequest) {
  const tokenKey = process.env.NEXT_PUBLIC_AUTH_TOKEN_KEY || DEFAULT_TOKEN_KEY;
  const token = request.cookies.get(tokenKey)?.value;
  const { pathname } = request.nextUrl;

  const isPublicPath = PUBLIC_PATHS.some((p) => pathname.startsWith(p));

  if (!token && !isPublicPath) {
    const loginUrl = new URL("/login", request.url);
    loginUrl.searchParams.set("next", pathname);
    return NextResponse.redirect(loginUrl);
  }

  if (token && isPublicPath) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    /*
     * 静的ファイル・APIルート等を除いた全パスに適用
     */
    "/((?!_next/static|_next/image|favicon.ico|api).*)",
  ],
};
