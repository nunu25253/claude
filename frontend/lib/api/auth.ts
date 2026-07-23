import { apiClient } from "../api-client";
import type {
  AuthResponse,
  EmailVerificationConfirmRequest,
  EmailVerificationResendRequest,
  LoginRequest,
  PasswordResetConfirmRequest,
  PasswordResetRequestRequest,
  RegisterRequest,
} from "../types";

export const authApi = {
  login: (payload: LoginRequest) =>
    apiClient.post<AuthResponse>("/auth/login", payload, { skipAuth: true }),

  register: (payload: RegisterRequest) =>
    apiClient.post<AuthResponse>("/auth/register", payload, {
      skipAuth: true,
    }),

  // ログアウトはリフレッシュトークン(Cookie)をサーバー側で失効させ、認証Cookieを削除するため
  // 実際にAPIを呼ぶ必要がある(HttpOnly CookieはフロントのJSから直接削除できない)。
  logout: () => apiClient.post<void>("/auth/logout", undefined, { skipAuth: true }),

  requestPasswordReset: (payload: PasswordResetRequestRequest) =>
    apiClient.post<void>("/auth/password-reset/request", payload, { skipAuth: true }),

  confirmPasswordReset: (payload: PasswordResetConfirmRequest) =>
    apiClient.post<void>("/auth/password-reset/confirm", payload, { skipAuth: true }),

  resendEmailVerification: (payload: EmailVerificationResendRequest) =>
    apiClient.post<void>("/auth/email-verification/resend", payload, { skipAuth: true }),

  confirmEmailVerification: (payload: EmailVerificationConfirmRequest) =>
    apiClient.post<void>("/auth/email-verification/confirm", payload, { skipAuth: true }),
};
