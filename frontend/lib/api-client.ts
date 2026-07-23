import { ApiError, type ApiErrorBody } from "./types/common";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

const CSRF_COOKIE_NAME = "XSRF-TOKEN";
const CSRF_HEADER_NAME = "X-XSRF-TOKEN";
const MUTATING_METHODS = new Set(["POST", "PUT", "PATCH", "DELETE"]);

// 複数のリクエストが同時に401/403を受け取っても /auth/refresh を1回しか呼ばないよう、
// 進行中のリフレッシュ処理を共有する（同時に呼ぶとリフレッシュトークンの二重消費で
// 片方が失敗する競合が起こりうるため）。
let inFlightRefresh: Promise<boolean> | null = null;

function readCookie(name: string): string | null {
  if (typeof document === "undefined") return null;
  const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`));
  return match?.[1] ? decodeURIComponent(match[1]) : null;
}

async function refreshAccessToken(): Promise<boolean> {
  if (inFlightRefresh) return inFlightRefresh;

  inFlightRefresh = (async () => {
    try {
      // リフレッシュトークンはHttpOnly Cookieとして自動送信されるため、
      // リクエストボディに何も乗せる必要が無い。
      const res = await fetch(new URL("auth/refresh", `${API_BASE_URL}/`).toString(), {
        method: "POST",
        credentials: "include",
      });
      return res.ok;
    } catch {
      return false;
    }
  })();

  try {
    return await inFlightRefresh;
  } finally {
    inFlightRefresh = null;
  }
}

export interface RequestOptions {
  /** クエリパラメータ。undefined の値は自動的に除外される */
  params?: Record<string, string | number | boolean | undefined>;
  /** 401時の自動リフレッシュ&リトライを行わない場合（login/register/refresh/logout用）に true */
  skipAuth?: boolean;
  signal?: AbortSignal;
}

function buildUrl(path: string, params?: object): string {
  const url = new URL(
    path.startsWith("/") ? path.slice(1) : path,
    `${API_BASE_URL}/`,
  );
  if (params) {
    for (const [key, value] of Object.entries(params as Record<string, unknown>)) {
      if (value !== undefined && value !== null && value !== "") {
        url.searchParams.set(key, String(value));
      }
    }
  }
  return url.toString();
}

async function parseErrorBody(res: Response): Promise<Partial<ApiErrorBody>> {
  try {
    const data = await res.json();
    return data as Partial<ApiErrorBody>;
  } catch {
    return { message: res.statusText };
  }
}

/**
 * 状態変更リクエストの直前にXSRF-TOKEN Cookieを取得し直す。
 * (実測により判明した挙動: バックエンドのSpring Security CSRF設定では、認証済みリクエストが
 * 1件処理されるたびにXSRF-TOKEN Cookieが失効し、明示的に/auth/csrfへアクセスしないと
 * 再発行されない。そのためログイン直後に一度取得しただけのCookieは、その後に別の
 * 認証付きGETが1件でも挟まると使えなくなる。ここでは正確性を優先し、状態変更リクエストの
 * 都度フレッシュなトークンを取りに行く。ユーザー操作起点の呼び出しであり高頻度アクセスでは
 * ないため、追加の1往復のレイテンシは実用上問題にならない。)
 */
async function ensureFreshCsrfCookie(): Promise<void> {
  try {
    await fetch(new URL("auth/csrf", `${API_BASE_URL}/`).toString(), {
      method: "GET",
      credentials: "include",
    });
  } catch {
    // 取得に失敗しても、その後の本リクエストが403で失敗する形で表面化するため、ここでは無視する。
  }
}

/**
 * fetch をラップした薄いAPIクライアント。
 * - 認証はHttpOnly Cookie(access_token/refresh_token)をブラウザが自動送信するため、
 *   このクライアント自身はトークンに一切触れない(credentials: "include"を付与するのみ)。
 * - 状態変更リクエスト(POST/PUT/PATCH/DELETE)にはCSRF対策としてXSRF-TOKEN Cookieの値を
 *   X-XSRF-TOKENヘッダーへ複製して送る(バックエンドのCookieCsrfTokenRepositoryと対になる
 *   ダブルサブミットCookie方式。攻撃者のクロスサイトページは同一オリジンポリシーにより
 *   このCookie値を読めないため偽造できない)。
 * - 非2xxレスポンスは ApiError に変換して throw する
 * - バックエンド未起動時などのネットワークエラーも ApiError に統一する
 */
async function request<T>(
  method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE",
  path: string,
  body?: unknown,
  options: RequestOptions = {},
): Promise<T> {
  const headers: Record<string, string> = {
    Accept: "application/json",
  };

  if (body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  if (MUTATING_METHODS.has(method) && !options.skipAuth) {
    await ensureFreshCsrfCookie();
    const csrfToken = readCookie(CSRF_COOKIE_NAME);
    if (csrfToken) {
      headers[CSRF_HEADER_NAME] = csrfToken;
    }
  }

  const doFetch = () =>
    fetch(buildUrl(path, options.params), {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: options.signal,
      cache: "no-store",
      credentials: "include",
    });

  let res: Response;
  try {
    res = await doFetch();
  } catch {
    // バックエンドに到達できない場合（起動していない・ネットワーク断など）
    throw new ApiError(0, {
      message:
        "サーバーに接続できませんでした。バックエンドAPIが起動しているか確認してください。",
    });
  }

  // アクセストークン失効時は一度だけリフレッシュして同じリクエストをやり直す。
  // login/register/refresh/logout自体（skipAuth）はここでの再試行対象にしない。
  if ((res.status === 401 || res.status === 403) && !options.skipAuth) {
    const refreshed = await refreshAccessToken();
    if (refreshed) {
      try {
        res = await doFetch();
      } catch {
        throw new ApiError(0, {
          message:
            "サーバーに接続できませんでした。バックエンドAPIが起動しているか確認してください。",
        });
      }
    }
  }

  if (!res.ok) {
    const errorBody = await parseErrorBody(res);
    throw new ApiError(res.status, errorBody);
  }

  if (res.status === 204) {
    return undefined as T;
  }

  const contentType = res.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) {
    // PDF/HTML等バイナリ・非JSONレスポンスはそのまま呼び出し元で扱えるよう Response を返せないため
    // ここでは blob をキャストして返す（reports API 等で利用）
    return (await res.blob()) as unknown as T;
  }

  return (await res.json()) as T;
}

export const apiClient = {
  get: <T>(path: string, options?: RequestOptions) => request<T>("GET", path, undefined, options),
  post: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>("POST", path, body, options),
  put: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>("PUT", path, body, options),
  patch: <T>(path: string, body?: unknown, options?: RequestOptions) =>
    request<T>("PATCH", path, body, options),
  delete: <T>(path: string, options?: RequestOptions) =>
    request<T>("DELETE", path, undefined, options),
};

/**
 * PostSearchParams のような具体的な型（index signature を持たない）を
 * apiClient の params（index signature 付きの Record 型）に渡すためのヘルパー。
 * スプレッドでフレッシュなオブジェクトリテラルにすることで型チェックを通す。
 */
export function toQueryParams<T extends object>(
  params: T,
): Record<string, string | number | boolean | undefined> {
  return { ...params } as Record<string, string | number | boolean | undefined>;
}

export { API_BASE_URL };
