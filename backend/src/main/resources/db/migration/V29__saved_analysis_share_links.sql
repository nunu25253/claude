-- 保存済み分析を未ログインの外部クライアントに閲覧専用で共有するためのリンク
-- (シニアレビュー: 「外部クライアント向けの閲覧専用共有リンクが無い」への対応)。
CREATE TABLE saved_analysis_share_links (
    id UUID PRIMARY KEY,
    saved_analysis_id UUID NOT NULL REFERENCES saved_analyses (id) ON DELETE CASCADE,
    created_by_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX idx_saved_analysis_share_links_saved_analysis_id ON saved_analysis_share_links (saved_analysis_id);
