import { describe, expect, it, vi, beforeEach } from "vitest";
import { act, renderHook, waitFor } from "@testing-library/react";
import { AuthProvider, useAuth } from "./auth-context";
import { authApi } from "@/lib/api";

vi.mock("@/lib/api", () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
    logout: vi.fn(),
    deleteAccount: vi.fn(),
  },
}));

const pushMock = vi.fn();
vi.mock("next/navigation", () => ({
  useRouter: () => ({ push: pushMock }),
}));

const STORAGE_KEY = "sns_buzz_auth_user";

const authResponse = {
  userId: "user-1",
  email: "user@example.com",
  displayName: "Test User",
  emailVerified: false,
  accessToken: "access-token",
  accessTokenExpiresInSeconds: 1800,
  refreshToken: "refresh-token",
  refreshTokenExpiresInSeconds: 1209600,
};

describe("AuthProvider / useAuth", () => {
  beforeEach(() => {
    window.localStorage.clear();
    vi.mocked(authApi.login).mockReset();
    vi.mocked(authApi.register).mockReset();
    vi.mocked(authApi.logout).mockReset();
    vi.mocked(authApi.deleteAccount).mockReset();
    pushMock.mockReset();
  });

  it("throws when useAuth is used outside AuthProvider", () => {
    expect(() => renderHook(() => useAuth())).toThrow(
      "useAuth は AuthProvider の内側で使用してください",
    );
  });

  it("starts uninitialized with no user, then finishes initializing with no stored user", async () => {
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await waitFor(() => expect(result.current.isInitializing).toBe(false));
    expect(result.current.user).toBeNull();
    expect(result.current.isAuthenticated).toBe(false);
  });

  it("restores the user from localStorage on mount", async () => {
    window.localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({ id: "user-1", email: "user@example.com", displayName: "Test User" }),
    );

    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await waitFor(() => expect(result.current.isInitializing).toBe(false));
    expect(result.current.user?.email).toBe("user@example.com");
    expect(result.current.isAuthenticated).toBe(true);
  });

  it("ignores malformed JSON in localStorage instead of throwing", async () => {
    window.localStorage.setItem(STORAGE_KEY, "{not-valid-json");

    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });

    await waitFor(() => expect(result.current.isInitializing).toBe(false));
    expect(result.current.user).toBeNull();
  });

  it("login stores the returned user in state and localStorage", async () => {
    vi.mocked(authApi.login).mockResolvedValue(authResponse);
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));

    await act(async () => {
      await result.current.login({ email: "user@example.com", password: "Password123!" });
    });

    expect(result.current.user).toEqual({
      id: "user-1",
      email: "user@example.com",
      displayName: "Test User",
      emailVerified: false,
    });
    expect(result.current.isAuthenticated).toBe(true);
    expect(JSON.parse(window.localStorage.getItem(STORAGE_KEY)!)).toEqual(result.current.user);
  });

  it("register stores the returned user in state and localStorage", async () => {
    vi.mocked(authApi.register).mockResolvedValue({ ...authResponse, userId: "user-2" });
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));

    await act(async () => {
      await result.current.register({
        email: "user@example.com",
        password: "Password123!",
        displayName: "Test User",
      });
    });

    expect(result.current.user?.id).toBe("user-2");
  });

  it("login rejects and does not set a user when the API call fails", async () => {
    vi.mocked(authApi.login).mockRejectedValue(new Error("invalid credentials"));
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));

    await expect(
      act(async () => {
        await result.current.login({ email: "user@example.com", password: "wrong" });
      }),
    ).rejects.toThrow("invalid credentials");

    expect(result.current.user).toBeNull();
    expect(window.localStorage.getItem(STORAGE_KEY)).toBeNull();
  });

  it("logout clears user/localStorage and redirects to /login even when the API call succeeds", async () => {
    vi.mocked(authApi.login).mockResolvedValue(authResponse);
    vi.mocked(authApi.logout).mockResolvedValue(undefined);
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));
    await act(async () => {
      await result.current.login({ email: "user@example.com", password: "Password123!" });
    });

    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.user).toBeNull();
    expect(window.localStorage.getItem(STORAGE_KEY)).toBeNull();
    expect(pushMock).toHaveBeenCalledWith("/login");
  });

  it("logout still clears client-side state and redirects when the API call fails", async () => {
    vi.mocked(authApi.login).mockResolvedValue(authResponse);
    vi.mocked(authApi.logout).mockRejectedValue(new Error("network error"));
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));
    await act(async () => {
      await result.current.login({ email: "user@example.com", password: "Password123!" });
    });

    await act(async () => {
      await result.current.logout();
    });

    expect(result.current.user).toBeNull();
    expect(window.localStorage.getItem(STORAGE_KEY)).toBeNull();
    expect(pushMock).toHaveBeenCalledWith("/login");
  });

  it("markEmailVerified sets emailVerified to true on the current user and persists it", async () => {
    vi.mocked(authApi.login).mockResolvedValue(authResponse);
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));
    await act(async () => {
      await result.current.login({ email: "user@example.com", password: "Password123!" });
    });
    expect(result.current.user?.emailVerified).toBe(false);

    act(() => {
      result.current.markEmailVerified();
    });

    expect(result.current.user?.emailVerified).toBe(true);
    expect(JSON.parse(window.localStorage.getItem(STORAGE_KEY)!).emailVerified).toBe(true);
  });

  it("deleteAccount clears user/localStorage and does a full navigation to /welcome on success", async () => {
    // DashboardLayoutの認証ガード(useEffectでisAuthenticated===falseなら/loginへ強制遷移)との
    // 競合を避けるため、deleteAccountはNext.jsのクライアントサイドrouter.pushではなく
    // window.location.hrefによるフルナビゲーションを使う(auth-context.tsx参照)。
    // そのためこのテストではpushMockではなくwindow.location.hrefへの代入を検証する。
    const originalLocation = window.location;
    Object.defineProperty(window, "location", {
      configurable: true,
      value: { ...originalLocation, href: "" },
    });

    vi.mocked(authApi.login).mockResolvedValue(authResponse);
    vi.mocked(authApi.deleteAccount).mockResolvedValue(undefined);
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));
    await act(async () => {
      await result.current.login({ email: "user@example.com", password: "Password123!" });
    });

    await act(async () => {
      await result.current.deleteAccount("Password123!");
    });

    // window.location.hrefへの代入はフルページ遷移(ドキュメント全体の破棄)を伴うため、
    // 実際にはこの後React側の状態を気にする必要はない。ここではlocalStorageのクリアと
    // 遷移先の指定のみを検証する。
    expect(authApi.deleteAccount).toHaveBeenCalledWith({ currentPassword: "Password123!" });
    expect(window.localStorage.getItem(STORAGE_KEY)).toBeNull();
    expect(window.location.href).toBe("/welcome");

    Object.defineProperty(window, "location", {
      configurable: true,
      value: originalLocation,
    });
  });

  it("deleteAccount rejects and keeps the current user when the API call fails", async () => {
    vi.mocked(authApi.login).mockResolvedValue(authResponse);
    vi.mocked(authApi.deleteAccount).mockRejectedValue(new Error("invalid password"));
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));
    await act(async () => {
      await result.current.login({ email: "user@example.com", password: "Password123!" });
    });

    await expect(
      act(async () => {
        await result.current.deleteAccount("wrong-password");
      }),
    ).rejects.toThrow("invalid password");

    expect(result.current.user).not.toBeNull();
    expect(window.localStorage.getItem(STORAGE_KEY)).not.toBeNull();
  });

  it("markEmailVerified is a no-op when there is no current user", async () => {
    const { result } = renderHook(() => useAuth(), { wrapper: AuthProvider });
    await waitFor(() => expect(result.current.isInitializing).toBe(false));

    act(() => {
      result.current.markEmailVerified();
    });

    expect(result.current.user).toBeNull();
  });
});
