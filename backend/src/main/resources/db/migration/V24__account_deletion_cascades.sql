-- アカウント削除機能(改善計画: 個人情報保護法上のユーザーの権利対応)のための下準備。
-- users行を削除した際、関連する個人データを自動的に整理する。
-- reports.user_idのみ「誰が生成したか」の付随情報でありレポート自体は投稿に紐づく
-- 共有可能なデータのため、ON DELETE SET NULLとしてレポート自体は保持する。
-- それ以外は明確にアカウント固有のデータのため、ON DELETE CASCADEで一緒に削除する。
-- (user_settingsは既存マイグレーション(V15)で既にON DELETE CASCADE済み)

ALTER TABLE reports
    DROP CONSTRAINT reports_user_id_fkey,
    ADD CONSTRAINT reports_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE password_reset_tokens
    DROP CONSTRAINT password_reset_tokens_user_id_fkey,
    ADD CONSTRAINT password_reset_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE email_verification_tokens
    DROP CONSTRAINT email_verification_tokens_user_id_fkey,
    ADD CONSTRAINT email_verification_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE saved_analyses
    DROP CONSTRAINT saved_analyses_user_id_fkey,
    ADD CONSTRAINT saved_analyses_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE organization_memberships
    DROP CONSTRAINT organization_memberships_user_id_fkey,
    ADD CONSTRAINT organization_memberships_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE subscriptions
    DROP CONSTRAINT subscriptions_user_id_fkey,
    ADD CONSTRAINT subscriptions_user_id_fkey FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
