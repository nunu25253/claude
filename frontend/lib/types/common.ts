/**
 * アプリ全体で共有する共通型定義。
 * バックエンド（Spring Boot）のレスポンス構造に合わせている。
 */

// 対応SNSプラットフォーム
export type Platform = "INSTAGRAM" | "TIKTOK" | "X";

// 投稿ジャンル（バックエンドの分類に準拠。値が増減しても UI が壊れないよう string も許容）
export type Genre =
  | "BEAUTY"
  | "FASHION"
  | "FOOD"
  | "TRAVEL"
  | "FITNESS"
  | "ENTERTAINMENT"
  | "TECH"
  | "LIFESTYLE"
  | "EDUCATION"
  | "BUSINESS"
  | "OTHER"
  | (string & {});

// Spring Data の Page<T> レスポンスに対応するページネーション型
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number; // 現在のページ番号（0始まり）
  size: number;
  first: boolean;
  last: boolean;
}

// バックエンドの汎用エラーレスポンス
export interface ApiErrorBody {
  status?: number;
  code?: string;
  message: string;
  errors?: Record<string, string>;
}

// API クライアントが投げるエラー。UI 側でエラーメッセージ/ステータスを判定するために使う
export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly fieldErrors?: Record<string, string>;

  constructor(status: number, body: Partial<ApiErrorBody> | undefined) {
    super(body?.message ?? `APIリクエストに失敗しました (status: ${status})`);
    this.name = "ApiError";
    this.status = status;
    this.code = body?.code;
    this.fieldErrors = body?.errors;
  }
}

export type SortDirection = "asc" | "desc";

export interface PageRequest {
  page?: number;
  size?: number;
  sort?: string;
}
