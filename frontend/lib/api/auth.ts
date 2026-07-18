import { apiClient } from "../api-client";
import type { AuthResponse, LoginRequest, RegisterRequest } from "../types";

export const authApi = {
  login: (payload: LoginRequest) =>
    apiClient.post<AuthResponse>("/auth/login", payload, { skipAuth: true }),

  register: (payload: RegisterRequest) =>
    apiClient.post<AuthResponse>("/auth/register", payload, {
      skipAuth: true,
    }),
};
