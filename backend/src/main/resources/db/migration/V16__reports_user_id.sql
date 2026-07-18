-- レポート履歴一覧(GET /reports)のためにレポート生成者を記録する。
-- 既存行(移行前に生成されたレポート)にはユーザーが紐付かないためNULL許容とする。
ALTER TABLE reports
    ADD COLUMN user_id UUID REFERENCES users (id);

CREATE INDEX idx_reports_user_id ON reports (user_id);
