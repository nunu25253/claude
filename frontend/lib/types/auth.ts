import type { components } from "./generated/api";

// バックエンドの実装(Controller/DTO)からspringdocが生成したOpenAPIスキーマを
// `npm run generate:api-types` でTypeScript化したものを型の一次ソースとする。
// docs/openapi.yamlの手動同期やフロント側の手書き型と実装がズレる問題への対応(改善計画No.19)。
// バックエンドでフィールドの追加・削除・リネームがあれば、この型を使っている箇所が
// コンパイルエラーとして検出される。
type Schemas = components["schemas"];

export type LoginRequest = Schemas["LoginRequest"];
export type RegisterRequest = Schemas["RegisterRequest"];
export type PasswordResetRequestRequest = Schemas["PasswordResetRequestRequest"];
export type PasswordResetConfirmRequest = Schemas["PasswordResetConfirmRequest"];
export type EmailVerificationResendRequest = Schemas["EmailVerificationResendRequest"];
export type EmailVerificationConfirmRequest = Schemas["EmailVerificationConfirmRequest"];

// springdocはJavaレコードのレスポンスDTOを全フィールドoptional扱いで出力するため、
// 実際には必ず値が入る(AuthApplicationService#issueTokensが全フィールドを設定する)前提で
// Requiredを被せる。フィールド自体の型・存在チェックはSchemas["AuthResult"]由来のまま。
export type AuthResponse = Required<Schemas["AuthResult"]>;

// フロントエンドでの認証状態管理用のローカルモデル(APIレスポンスそのものではない)。
export interface User {
  id: string;
  email: string;
  displayName: string;
  emailVerified?: boolean;
  createdAt?: string;
}
