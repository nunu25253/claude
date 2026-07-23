import { afterEach, describe, expect, it, vi } from "vitest";
import { downloadCsv, toCsv } from "./csv";

describe("toCsv", () => {
  it("joins headers and rows with CRLF", () => {
    const result = toCsv(["名前", "スコア"], [["投稿A", 80]]);
    expect(result).toBe("名前,スコア\r\n投稿A,80");
  });

  it("quotes fields containing commas", () => {
    expect(toCsv(["col"], [["a,b"]])).toBe('col\r\n"a,b"');
  });

  it("quotes and escapes embedded double quotes", () => {
    expect(toCsv(["col"], [['say "hi"']])).toBe('col\r\n"say ""hi"""');
  });

  it("quotes fields containing newlines", () => {
    expect(toCsv(["col"], [["line1\nline2"]])).toBe('col\r\n"line1\nline2"');
  });

  it("leaves plain fields unquoted", () => {
    expect(toCsv(["a", "b"], [["1", "2"]])).toBe("a,b\r\n1,2");
  });

  it("handles multiple rows", () => {
    expect(toCsv(["h"], [["a"], ["b"]])).toBe("h\r\na\r\nb");
  });
});

describe("downloadCsv", () => {
  afterEach(() => {
    vi.restoreAllMocks();
    document.body.innerHTML = "";
  });

  it("creates a UTF-8 BOM-prefixed blob URL and triggers a download via a temporary anchor", () => {
    const createObjectURL = vi.fn((_blob: Blob) => "blob:mock-url");
    const revokeObjectURL = vi.fn();
    // jsdom は URL.createObjectURL / revokeObjectURL を実装していないため差し替える
    (URL as unknown as { createObjectURL: typeof createObjectURL }).createObjectURL = createObjectURL;
    (URL as unknown as { revokeObjectURL: typeof revokeObjectURL }).revokeObjectURL = revokeObjectURL;

    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, "click").mockImplementation(() => {});

    downloadCsv("report.csv", "a,b\r\n1,2");

    expect(createObjectURL).toHaveBeenCalledTimes(1);
    const call = createObjectURL.mock.calls[0];
    expect(call?.[0]).toBeInstanceOf(Blob);
    expect(call?.[0].type).toBe("text/csv;charset=utf-8;");
    expect(clickSpy).toHaveBeenCalledTimes(1);
    expect(revokeObjectURL).toHaveBeenCalledWith("blob:mock-url");
    // 一時的に追加した<a>要素はダウンロード後にDOMから除去される
    expect(document.body.querySelector("a")).toBeNull();
  });
});
