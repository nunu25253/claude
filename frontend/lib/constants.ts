import type { Genre, Platform } from "./types";

export const PLATFORM_OPTIONS: { value: Platform; label: string }[] = [
  { value: "INSTAGRAM", label: "Instagram" },
  { value: "TIKTOK", label: "TikTok" },
  { value: "X", label: "X (旧Twitter)" },
];

export const GENRE_OPTIONS: { value: Genre; label: string }[] = [
  { value: "BEAUTY", label: "美容" },
  { value: "FASHION", label: "ファッション" },
  { value: "FOOD", label: "グルメ" },
  { value: "TRAVEL", label: "旅行" },
  { value: "FITNESS", label: "フィットネス" },
  { value: "ENTERTAINMENT", label: "エンタメ" },
  { value: "TECH", label: "テック・ガジェット" },
  { value: "LIFESTYLE", label: "ライフスタイル" },
  { value: "EDUCATION", label: "教育" },
  { value: "BUSINESS", label: "ビジネス" },
  { value: "OTHER", label: "その他" },
];

export const RANKING_TYPE_OPTIONS = [
  { value: "trending", label: "急上昇" },
  { value: "weekly", label: "週間" },
  { value: "monthly", label: "月間" },
] as const;

export const REPORT_FORMAT_OPTIONS = [
  { value: "pdf", label: "PDF" },
  { value: "markdown", label: "Markdown" },
  { value: "html", label: "HTML" },
] as const;
