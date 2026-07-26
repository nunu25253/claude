-- Slack Incoming Webhook連携(シニアレビュー: 通知チャネルがメールのみ、への対応)。
-- 未設定(NULL)ならSlack送信をスキップする。
ALTER TABLE user_settings ADD COLUMN slack_webhook_url VARCHAR(500);
