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
import { clearToken, getToken, setRefreshToken, setToken } from "./token";

const USER_STORAGE_KEY = "sns_buzz_auth_user";

interface AuthContextValue {
  user: User | null;
  isAuthenticated: boolean;
  isInitializing: boolean;
  login: (payload: LoginRequest) => Promise<void>;
  register: (payload: RegisterRequest) => Promise<void>;
  logout: () => void;
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
  // 初回マウント時に localStorage からトークン/ユーザー情報を復元するまでのローディング状態
  const [isInitializing, setIsInitializing] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const token = getToken();
    const storedUser = readStoredUser();
    if (token && storedUser) {
      setUser(storedUser);
    }
    setIsInitializing(false);
  }, []);

  const login = useCallback(async (payload: LoginRequest) => {
    const res = await authApi.login(payload);
    setToken(res.accessToken);
    setRefreshToken(res.refreshToken);
    const loggedInUser: User = { id: res.userId, email: res.email, displayName: res.displayName };
    storeUser(loggedInUser);
    setUser(loggedInUser);
  }, []);

  const register = useCallback(async (payload: RegisterRequest) => {
    const res = await authApi.register(payload);
    setToken(res.accessToken);
    setRefreshToken(res.refreshToken);
    const registeredUser: User = { id: res.userId, email: res.email, displayName: res.displayName };
    storeUser(registeredUser);
    setUser(registeredUser);
  }, []);

  const logout = useCallback(() => {
    clearToken();
    storeUser(null);
    setUser(null);
    router.push("/login");
  }, [router]);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: !!user,
      isInitializing,
      login,
      register,
      logout,
    }),
    [user, isInitializing, login, register, logout],
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
