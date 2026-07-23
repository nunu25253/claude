/** CSVの1フィールド値をRFC4180に沿ってエスケープする(カンマ・ダブルクォート・改行を含む場合のみ引用) */
function escapeCsvField(value: string): string {
  if (/[",\n\r]/.test(value)) {
    return `"${value.replace(/"/g, '""')}"`;
  }
  return value;
}

export function toCsv(headers: string[], rows: (string | number)[][]): string {
  const lines = [headers, ...rows].map((row) =>
    row.map((cell) => escapeCsvField(String(cell))).join(","),
  );
  return lines.join("\r\n");
}

/** Excel(日本語環境)で文字化けしないよう UTF-8 BOM を付与してダウンロードさせる */
export function downloadCsv(filename: string, csvContent: string): void {
  const blob = new Blob([`﻿${csvContent}`], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
