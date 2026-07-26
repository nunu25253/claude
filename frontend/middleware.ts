import { NextRequest, NextResponse } from "next/server";

// バックエンド(AuthCookieNames.ACCESS_TOKEN)が発行するHttpOnly Cookieの名前と同じ値。
// HttpOnlyはブラウザのJSからの読み取りを禁じるだけで、サーバーサイドのmiddlewareが
// リクエストヘッダー経由でCookieの有無を見ることは問題無い(実際の署名検証はバックエンド側で行う。
// ここでは「Cookieが存在するか」だけを見た簡易ガード)。
const ACCESS_TOKEN_COOKIE = "access_token";

// ログイン不要でアクセスできるパス
const PUBLIC_PATHS = [
  "/login",
  "/register",
  "/forgot-password",
  "/reset-password",
  "/verify-email",
  "/welcome",
  "/terms",
  "/privacy",
  "/legal",
  "/shared",
];
// ログイン済みならダッシュボードへ戻すパス(/verify-email は登録直後の
// ログイン済みユーザーもアクセスするため対象外とする)
const AUTH_ONLY_PATHS = ["/login", "/register", "/forgot-password", "/reset-password"];

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

// リクエスト毎にnonceを生成しscript-src/style-srcの'unsafe-inline'を排除する。
// script-srcは'strict-dynamic'を併用し、nonce付きスクリプトが動的に読み込むスクリプトも
// 許可することで、Next.js自身がハイドレーション用に注入するインラインスクリプトを
// (x-nonceリクエストヘッダー経由で)個別許可せず自動的に通す。
function buildCspHeader(nonce: string): string {
  return [
    "default-src 'self'",
    // SNS投稿のサムネイル/アバターは外部ドメインの画像を表示するためhttps全般を許可する
    "img-src 'self' https: data:",
    `script-src 'self' 'nonce-${nonce}' 'strict-dynamic'${process.env.NODE_ENV === "production" ? "" : " 'unsafe-eval'"}`,
    `style-src 'self' 'nonce-${nonce}'`,
    // GMO-PGのカードトークン化JS(gmo-card-token-form.tsx)がトークン発行のために呼び出す
    // 先。バックエンドのGmoPaymentGatewayAdapterと同様、実契約での疎通確認は行っていない
    // ドメインのため、契約時に提供される技術仕様書と突き合わせて要修正。
    `connect-src 'self' ${apiOrigin()} https://static.mul-pay.jp`.trim(),
    "object-src 'none'",
    "base-uri 'self'",
    "frame-ancestors 'none'",
  ].join("; ");
}

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

  const nonce = Buffer.from(crypto.randomUUID()).toString("base64");
  const cspHeader = buildCspHeader(nonce);

  const requestHeaders = new Headers(request.headers);
  requestHeaders.set("x-nonce", nonce);

  const response = NextResponse.next({ request: { headers: requestHeaders } });
  response.headers.set("Content-Security-Policy", cspHeader);
  return response;
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
