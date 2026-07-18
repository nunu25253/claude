-- ユーザー設定(通知設定・APIキー)。プロフィール(表示名/メール)は既存のusersテーブルを使うため
-- ここでは扱わない。1ユーザー1行(user_idが主キー兼FK)。
CREATE TABLE user_settings (
    user_id                      UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    email_on_analysis_complete   BOOLEAN NOT NULL DEFAULT TRUE,
    email_weekly_digest          BOOLEAN NOT NULL DEFAULT FALSE,
    email_trending_alert         BOOLEAN NOT NULL DEFAULT FALSE,
    api_key                      VARCHAR(255),
    api_key_created_at           TIMESTAMPTZ,
    created_at                   TIMESTAMPTZ NOT NULL,
    updated_at                   TIMESTAMPTZ NOT NULL
);
