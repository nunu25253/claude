export interface User {
  id: string;
  email: string;
  displayName: string;
  createdAt?: string;
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

// バックエンド(AuthResult)のレスポンス構造に合わせたフラットな形。
// ネストした user オブジェクトは返らないため、呼び出し側で User に組み立てる。
export interface AuthResponse {
  userId: string;
  email: string;
  displayName: string;
  accessToken: string;
  accessTokenExpiresInSeconds: number;
  refreshToken: string;
  refreshTokenExpiresInSeconds: number;
}
