"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useRouter } from "next/navigation";
import { authApi } from "@/lib/api";
import type {
  LoginRequest,
  RegisterRequest,
  User,
} from "@/lib/types";

const USER_STORAGE_KEY = "sns_buzz_auth_user";

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  deleteAccount: (currentPassword: string) => Promise<void>;
  markEmailVerified: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readStoredUser(): User | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = window.localStorage.getItem(USER_STORAGE_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    return null;
  }
}

function storeUser(user: User | null) {
  if (typeof window === "undefined") return;
  if (user) {
    window.localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  } else {
    window.localStorage.removeItem(USER_STORAGE_KEY);
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  // 初回マウント時に localStorage からユーザー情報(非機密なプロフィールのみ)を復元するまでの
  // ローディング状態。アクセストークンはHttpOnly Cookieのためここでは検証できず、
  // 実際の認証有無はAPI呼び出し時(401)やミドルウェアのCookie存在チェックに委ねる
  // (HttpOnly Cookie採用によるトレードオフ。楽観的にキャッシュ済みユーザーを表示する)。
  const [isInitializing, setIsInitializing] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const storedUser = readStoredUser();
    if (storedUser) {
      setUser(storedUser);
    }
    setIsInitializing(false);
  }, []);

  const login = useCallback(async (payload: LoginRequest) => {
    const res = await authApi.login(payload);
    const loggedInUser: User = {
      id: res.userId,
      email: res.email,
      displayName: res.displayName,
      emailVerified: res.emailVerified,
    };
    storeUser(loggedInUser);
    setUser(loggedInUser);
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    const res = await authApi.register(payload);
    const registeredUser: User = {
      id: res.userId,
      email: res.email,
      displayName: res.displayName,
      emailVerified: res.emailVerified,
    };
    storeUser(registeredUser);
    setUser(registeredUser);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // バックエンドに到達できなくても、クライアント側の状態は必ずクリアしてログイン画面へ戻す。
    }
    storeUser(null);
    setUser(null);
    router.push("/login");
  }, [router]);

  // logout()と異なり、パスワード誤り等のAPIエラーは呼び出し元(削除フォーム)で
  // メッセージ表示する必要があるため、ここでは握りつぶさずそのまま伝播させる。
  // 成功時のみローカル状態のクリア・遷移を行う。
  const deleteAccount = useCallback(async (currentPassword: string) => {
    await authApi.deleteAccount({ currentPassword });
    storeUser(null);
    // DashboardLayoutはisAuthenticatedがfalseになった時点で/loginへ強制遷移するガードを持つ。
    // ここでsetUser(null)してからNext.jsのクライアントサイドrouter.push("/welcome")を呼ぶと、
    // このガードのuseEffectが先に走り/loginへ奪われる競合が発生する(router.pushはReactの
    // トランジションとして低優先度で処理されるのに対し、setUserは通常優先度で即座に
    // 再レンダリングされガードのeffectを先に発火させてしまうため)。
    // アカウント削除は取り消し不能な最終操作であり、この後アプリの状態を保持し続ける必要はないため、
    // ブラウザのフルナビゲーションで確実に/welcomeへ遷移し、あわせてクライアント側の状態
    // (メモリ上のReact/React Queryキャッシュ等)も完全に破棄する。
    window.location.href = "/welcome";
  }, []);

  const markEmailVerified = useCallback(() => {
    setUser((current) => {
      if (!current) return current;
      const updated = { ...current, emailVerified: true };
      storeUser(updated);
      return updated;
    });
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: !!user,
      isInitializing,
      login,
      register,
      logout,
      deleteAccount,
      markEmailVerified,
    }),
    [user, isInitializing, login, register, logout, deleteAccount, markEmailVerified],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth は AuthProvider の内側で使用してください");
  }
  return ctx;
}
