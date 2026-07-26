import { describe, expect, it } from "vitest";
import {
  buzzScoreColor,
  formatCompactNumber,
  formatDate,
  formatDateTime,
  formatDurationSeconds,
  formatNumber,
  formatPercent,
} from "./utils";

describe("formatCompactNumber", () => {
  it("returns '-' for null/undefined/NaN", () => {
    expect(formatCompactNumber(null)).toBe("-");
    expect(formatCompactNumber(undefined)).toBe("-");
    expect(formatCompactNumber(Number.NaN)).toBe("-");
  });

  it("formats large numbers using Japanese 万-based compact notation", () => {
    // ja-JPロケールのcompact記法は英語のK/M/Bではなく万(10,000)単位で切り替わる
    expect(formatCompactNumber(1200)).toBe("1200");
    expect(formatCompactNumber(12000)).toBe("1.2万");
    expect(formatCompactNumber(1200000)).toBe("120万");
    expect(formatCompactNumber(0)).toBe("0");
  });
});

describe("formatNumber", () => {
  it("returns '-' for null/undefined/NaN", () => {
    expect(formatNumber(null)).toBe("-");
    expect(formatNumber(undefined)).toBe("-");
    expect(formatNumber(Number.NaN)).toBe("-");
  });

  it("formats with thousands separators", () => {
    expect(formatNumber(1234567)).toBe("1,234,567");
  });
});

describe("formatPercent", () => {
  it("returns '-' for null/undefined/NaN", () => {
    expect(formatPercent(null)).toBe("-");
    expect(formatPercent(undefined)).toBe("-");
    expect(formatPercent(Number.NaN)).toBe("-");
  });

  it("converts a 0-1 ratio to a percentage string with default 1 digit", () => {
    expect(formatPercent(0.5)).toBe("50.0%");
  });

  it("respects a custom digits argument", () => {
    expect(formatPercent(0.12345, 2)).toBe("12.35%");
  });
});

describe("formatDateTime / formatDate", () => {
  it("returns '-' for falsy input", () => {
    expect(formatDateTime(null)).toBe("-");
    expect(formatDateTime(undefined)).toBe("-");
    expect(formatDateTime("")).toBe("-");
    expect(formatDate(null)).toBe("-");
  });

  it("returns '-' for an unparseable date string", () => {
    expect(formatDateTime("not-a-date")).toBe("-");
    expect(formatDate("not-a-date")).toBe("-");
  });

  it("formats a valid ISO date string", () => {
    // ja-JP の Intl.DateTimeFormat 出力はロケールデータに依存しうるため、
    // 年/月/日の各要素が含まれていることのみを検証する(区切り文字は問わない)。
    const result = formatDate("2026-03-05T10:00:00Z");
    expect(result).toContain("2026");
    expect(result).toContain("03");
    expect(result).toContain("05");
  });
});

describe("formatDurationSeconds", () => {
  it("returns '-' for null/undefined", () => {
    expect(formatDurationSeconds(null)).toBe("-");
    expect(formatDurationSeconds(undefined)).toBe("-");
  });

  it("formats sub-minute durations as seconds only", () => {
    expect(formatDurationSeconds(45)).toBe("45秒");
  });

  it("formats durations of a minute or more as minutes and seconds", () => {
    expect(formatDurationSeconds(90)).toBe("1分30秒");
  });

  it("rounds fractional seconds", () => {
    expect(formatDurationSeconds(61.6)).toBe("1分2秒");
  });
});

describe("buzzScoreColor", () => {
  it("maps score ranges to the expected buzz color tiers", () => {
    expect(buzzScoreColor(85)).toBe("text-buzz-top");
    expect(buzzScoreColor(80)).toBe("text-buzz-top");
    expect(buzzScoreColor(70)).toBe("text-buzz-high");
    expect(buzzScoreColor(60)).toBe("text-buzz-high");
    expect(buzzScoreColor(50)).toBe("text-buzz-mid");
    expect(buzzScoreColor(40)).toBe("text-buzz-mid");
    expect(buzzScoreColor(0)).toBe("text-buzz-low");
  });
});
