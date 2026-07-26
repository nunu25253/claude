-- 登録時のメールアドレス所有確認。他人のメールアドレスでの登録(なりすまし)対策として、
-- 未確認アカウントはOpenAI呼び出しを伴う投稿分析(コスト発生エンドポイント)を制限する。
-- 既存ユーザーは確認済み扱いとし(DEFAULT true でバックフィル)、以後の新規登録は
-- アプリケーション側で明示的にfalseを設定する(このDEFAULTは将来アプリを経由しない
-- 手動INSERTがあった場合の安全側フォールバックとしてfalseに変更しておく)。
ALTER TABLE users ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE users ALTER COLUMN email_verified SET DEFAULT false;

CREATE TABLE email_verification_tokens (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ NOT NULL,
    used_at     TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_email_verification_tokens_user_id ON email_verification_tokens(user_id);
