import { clsx, type ClassValue } from "clsx";

/** Tailwind クラス名を条件付きで結合するためのヘルパー */
export function cn(...inputs: ClassValue[]): string {
  return clsx(inputs);
}

/** 1200 -> "1.2K" のような日本語圏でも読みやすい概数表記に変換 */
export function formatCompactNumber(value: number | undefined | null): string {
  if (value === undefined || value === null || Number.isNaN(value)) return "-";
  return new Intl.NumberFormat("ja-JP", {
    notation: "compact",
    maximumFractionDigits: 1,
  }).format(value);
}

export function formatNumber(value: number | undefined | null): string {
  if (value === undefined || value === null || Number.isNaN(value)) return "-";
  return new Intl.NumberFormat("ja-JP").format(value);
}

export function formatPercent(value: number | undefined | null, digits = 1): string {
  if (value === undefined || value === null || Number.isNaN(value)) return "-";
  return `${(value * 100).toFixed(digits)}%`;
}

export function formatDateTime(iso: string | undefined | null): string {
  if (!iso) return "-";
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return "-";
  return new Intl.DateTimeFormat("ja-JP", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

export function formatDate(iso: string | undefined | null): string {
  if (!iso) return "-";
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return "-";
  return new Intl.DateTimeFormat("ja-JP", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  }).format(date);
}

export function formatDurationSeconds(seconds: number | undefined | null): string {
  if (seconds === undefined || seconds === null) return "-";
  const m = Math.floor(seconds / 60);
  const s = Math.round(seconds % 60);
  return m > 0 ? `${m}分${s}秒` : `${s}秒`;
}

/** BuzzScore(0-100) から表示色を決める。UIの各所で使い回すため一箇所に集約 */
export function buzzScoreColor(score: number): string {
  if (score >= 80) return "text-buzz-top";
  if (score >= 60) return "text-buzz-high";
  if (score >= 40) return "text-buzz-mid";
  return "text-buzz-low";
}
