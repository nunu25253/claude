import { ApiError, type ApiErrorBody } from "./types/common";
import { getToken } from "./auth/token";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

export interface RequestOptions {
  /** クエリパラメータ。undefined の値は自動的に除外される */
  params?: Record<string, string | number | boolean | undefined>;
  /** 認証ヘッダーを付与しない場合（login/register 用）に true */
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
 * fetch をラップした薄いAPIクライアント。
 * - JWT を Authorization ヘッダーに自動付与
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

  if (!options.skipAuth) {
    const token = getToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
  }

  let res: Response;
  try {
    res = await fetch(buildUrl(path, options.params), {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal: options.signal,
      cache: "no-store",
    });
  } catch {
    // バックエンドに到達できない場合（起動していない・ネットワーク断など）
    throw new ApiError(0, {
      message:
        "サーバーに接続できませんでした。バックエンドAPIが起動しているか確認してください。",
    });
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
