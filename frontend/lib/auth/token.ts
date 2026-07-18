"use client";

/**
 * JWT トークンの保存・取得を担当するユーティリティ。
 * シンプルな実装として localStorage をメインに使い、
 * ミドルウェアでも参照できるよう同名の Cookie にも同期して保存する。
 */

const DEFAULT_TOKEN_KEY = "sns_buzz_auth_token";

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
  // middleware / SSR からも参照できるように Cookie にも保存（7日間）
  document.cookie = `${getTokenKey()}=${token}; path=/; max-age=${60 * 60 * 24 * 7}; SameSite=Lax`;
}

export function clearToken(): void {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(getTokenKey());
  document.cookie = `${getTokenKey()}=; path=/; max-age=0`;
}
