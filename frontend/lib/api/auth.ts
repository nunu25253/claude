import { apiClient } from "../api-client";
import type {
  AuthResponse,
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

  requestPasswordReset: (payload: PasswordResetRequestRequest) =>
    apiClient.post<void>("/auth/password-reset/request", payload, { skipAuth: true }),

  confirmPasswordReset: (payload: PasswordResetConfirmRequest) =>
    apiClient.post<void>("/auth/password-reset/confirm", payload, { skipAuth: true }),
};
