"use client";

/**
 * JWT トークンの保存・取得を担当するユーティリティ。
 * アクセストークン/リフレッシュトークンは localStorage に保存する。
 * ミドルウェアのルートガードはトークンの中身を検証せず存在有無しか見ないため、
 * Cookieには本物のトークンではなく意味を持たないフラグ値のみを保存する
 * （非HttpOnly CookieにJWT本体を複製するとXSS時の窃取面が不必要に広がるため）。
 */

const DEFAULT_TOKEN_KEY = "sns_buzz_auth_token";
const REFRESH_TOKEN_KEY = "sns_buzz_refresh_token";

function getTokenKey(): string {
  return process.env.NEXT_PUBLIC_AUTH_TOKEN_KEY || DEFAULT_TOKEN_KEY;
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(getTokenKey());
}

export function setToken(token: string): void {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(getTokenKey(), token);
  // middleware / SSR からログイン有無だけ判定できるよう、フラグ値のみをCookieに保存（7日間）
  document.cookie = `${getTokenKey()}=1; path=/; max-age=${60 * 60 * 24 * 7}; SameSite=Lax`;
}

export function clearToken(): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(getTokenKey());
  document.cookie = `${getTokenKey()}=; path=/; max-age=0`;
  clearRefreshToken();
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function setRefreshToken(token: string): void {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

export function clearRefreshToken(): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
}
