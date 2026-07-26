import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ApiError } from "./types/common";

const BASE_URL = "http://localhost:8080/api/v1";

/** jsdomはCookie jarをfetchに連動させないため、レスポンスヘッダーによるCookie設定を手動で模す */
function setCookie(nameValue: string) {
  document.cookie = nameValue;
}

function clearAllCookies() {
  document.cookie.split(";").forEach((c) => {
    const name = c.split("=")[0]?.trim();
    if (name) {
      document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
    }
  });
}

function mockResponse(init: {
  status: number;
  json?: () => Promise<unknown>;
  contentType?: string;
  blobValue?: Blob;
  statusText?: string;
}): Response {
  const ok = init.status >= 200 && init.status < 300;
  return {
    status: init.status,
    ok,
    statusText: init.statusText ?? "",
    headers: {
      get: (key: string) =>
        key.toLowerCase() === "content-type" ? (init.contentType ?? "application/json") : null,
    },
    json: init.json ?? (async () => ({})),
    blob: async () => init.blobValue ?? new Blob([]),
  } as unknown as Response;
}

describe("api-client", () => {
  let fetchMock: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    fetchMock = vi.fn();
    vi.stubGlobal("fetch", fetchMock);
    clearAllCookies();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
    clearAllCookies();
  });

  it("does not fetch a fresh CSRF cookie or attach X-XSRF-TOKEN for GET requests", async () => {
    setCookie("XSRF-TOKEN=should-not-be-sent");
    fetchMock.mockResolvedValueOnce(mockResponse({ status: 200, json: async () => ({ ok: true }) }));

    const { apiClient } = await import("./api-client");
    const result = await apiClient.get<{ ok: boolean }>("/posts");

    expect(result).toEqual({ ok: true });
    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(url).toBe(`${BASE_URL}/posts`);
    expect((init.headers as Record<string, string>)["X-XSRF-TOKEN"]).toBeUndefined();
    expect(init.credentials).toBe("include");
  });

  it("refreshes the CSRF cookie and attaches X-XSRF-TOKEN for mutating requests", async () => {
    fetchMock.mockImplementation(async (url: string) => {
      if (url === `${BASE_URL}/auth/csrf`) {
        setCookie("XSRF-TOKEN=fresh-token");
        return mockResponse({ status: 200 });
      }
      return mockResponse({ status: 200, json: async () => ({ created: true }) });
    });

    const { apiClient } = await import("./api-client");
    await apiClient.post("/posts/analyze", { url: "https://x.com/a" });

    expect(fetchMock).toHaveBeenCalledTimes(2);
    const [csrfUrl, csrfInit] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect(csrfUrl).toBe(`${BASE_URL}/auth/csrf`);
    expect(csrfInit.method).toBe("GET");

    const [, postInit] = fetchMock.mock.calls[1] as [string, RequestInit];
    expect((postInit.headers as Record<string, string>)["X-XSRF-TOKEN"]).toBe("fresh-token");
    expect(postInit.method).toBe("POST");
  });

  it("skips CSRF cookie refresh and header when skipAuth is true, even for mutating requests", async () => {
    fetchMock.mockResolvedValueOnce(mockResponse({ status: 200, json: async () => ({}) }));

    const { apiClient } = await import("./api-client");
    await apiClient.post("/auth/login", { email: "a@example.com" }, { skipAuth: true });

    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [, init] = fetchMock.mock.calls[0] as [string, RequestInit];
    expect((init.headers as Record<string, string>)["X-XSRF-TOKEN"]).toBeUndefined();
  });

  it("retries once via /auth/refresh on 401 and returns the retried response", async () => {
    let getCallCount = 0;
    fetchMock.mockImplementation(async (url: string) => {
      if (url === `${BASE_URL}/auth/refresh`) {
        return mockResponse({ status: 200 });
      }
      getCallCount += 1;
      if (getCallCount === 1) {
        return mockResponse({ status: 401, json: async () => ({ message: "unauthorized" }) });
      }
      return mockResponse({ status: 200, json: async () => ({ id: "1" }) });
    });

    const { apiClient } = await import("./api-client");
    const result = await apiClient.get<{ id: string }>("/settings/profile");

    expect(result).toEqual({ id: "1" });
    // 元のGET(401) + refresh + 元のGETの再試行(200) = 3回
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });

  it("throws ApiError with the original response's status/body when refresh fails after 401", async () => {
    fetchMock.mockImplementation(async (url: string) => {
      if (url === `${BASE_URL}/auth/refresh`) {
        return mockResponse({ status: 401 });
      }
      return mockResponse({ status: 401, json: async () => ({ message: "token expired" }) });
    });

    const { apiClient } = await import("./api-client");
    await expect(apiClient.get("/settings/profile")).rejects.toMatchObject({
      status: 401,
      message: "token expired",
    });
    // 元のGET(401) + refresh(失敗) = 2回。refresh失敗時は再試行しない
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });

  it("does not attempt a refresh on 401 when skipAuth is true", async () => {
    fetchMock.mockResolvedValueOnce(
      mockResponse({ status: 401, json: async () => ({ message: "invalid credentials" }) }),
    );

    const { apiClient } = await import("./api-client");
    await expect(apiClient.post("/auth/login", {}, { skipAuth: true })).rejects.toBeInstanceOf(ApiError);
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it("dedupes concurrent refresh calls into a single /auth/refresh request", async () => {
    let refreshCalls = 0;
    let callCountA = 0;
    let callCountB = 0;
    fetchMock.mockImplementation(async (url: string) => {
      if (url === `${BASE_URL}/auth/refresh`) {
        refreshCalls += 1;
        return mockResponse({ status: 200 });
      }
      const isA = url.endsWith("/a");
      const key = isA ? "a" : "b";
      const callCount = isA ? ++callCountA : ++callCountB;
      // 2つの並行リクエストは初回401、再試行は成功させる
      return callCount === 1
        ? mockResponse({ status: 401, json: async () => ({}) })
        : mockResponse({ status: 200, json: async () => ({ key }) });
    });

    const { apiClient } = await import("./api-client");
    const [resultA, resultB] = await Promise.all([apiClient.get("/a"), apiClient.get("/b")]);

    expect(resultA).toEqual({ key: "a" });
    expect(resultB).toEqual({ key: "b" });
    expect(refreshCalls).toBe(1);
  });

  it("wraps a network-level fetch failure in ApiError with status 0", async () => {
    fetchMock.mockRejectedValueOnce(new TypeError("Failed to fetch"));

    const { apiClient } = await import("./api-client");
    await expect(apiClient.get("/posts")).rejects.toMatchObject({ status: 0 });
  });

  it("returns undefined for a 204 No Content response", async () => {
    // DELETEはmutatingメソッドのためCSRFトークン再取得のfetchが先に走る
    fetchMock
      .mockResolvedValueOnce(mockResponse({ status: 200 }))
      .mockResolvedValueOnce(mockResponse({ status: 204 }));

    const { apiClient } = await import("./api-client");
    const result = await apiClient.delete("/posts/1");

    expect(result).toBeUndefined();
  });

  it("returns a Blob for non-JSON content-type responses", async () => {
    const pdfBlob = new Blob(["%PDF-1.4"], { type: "application/pdf" });
    fetchMock.mockResolvedValueOnce(
      mockResponse({ status: 200, contentType: "application/pdf", blobValue: pdfBlob }),
    );

    const { apiClient } = await import("./api-client");
    const result = await apiClient.get<Blob>("/reports/1/generate");

    expect(result).toBeInstanceOf(Blob);
    expect((result as Blob).type).toBe("application/pdf");
  });

  it("filters out undefined/null/empty-string query params but keeps other values", async () => {
    fetchMock.mockResolvedValueOnce(mockResponse({ status: 200, json: async () => ({}) }));

    const { apiClient } = await import("./api-client");
    await apiClient.get("/posts", {
      params: { genre: "BEAUTY", page: 0, keyword: "", missing: undefined, ignored: null as unknown as undefined },
    });

    const [url] = fetchMock.mock.calls[0] as [string];
    const parsed = new URL(url);
    expect(parsed.searchParams.get("genre")).toBe("BEAUTY");
    expect(parsed.searchParams.get("page")).toBe("0");
    expect(parsed.searchParams.has("keyword")).toBe(false);
    expect(parsed.searchParams.has("missing")).toBe(false);
    expect(parsed.searchParams.has("ignored")).toBe(false);
  });
});

describe("toQueryParams", () => {
  it("shallow-copies a concrete object into a params-compatible record", async () => {
    const { toQueryParams } = await import("./api-client");
    const input = { page: 1, size: 20 };
    const result = toQueryParams(input);
    expect(result).toEqual({ page: 1, size: 20 });
    expect(result).not.toBe(input);
  });
});
