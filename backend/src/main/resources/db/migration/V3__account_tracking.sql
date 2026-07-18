-- 定期データ取得バッチ（自動同期）の対象アカウントを管理するためのフラグ。
-- 既存アカウントはデフォルトで追跡対象(true)とする。
ALTER TABLE social_accounts
    ADD COLUMN tracking_enabled BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX idx_social_accounts_tracking_enabled ON social_accounts (tracking_enabled) WHERE tracking_enabled = TRUE;
