export interface User {
  id: string;
  email: string;
  displayName: string;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  displayName: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType?: string; // 通常 "Bearer"
  expiresIn?: number; // 秒
  user: User;
}
