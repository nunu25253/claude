-- Phase15: トレンド分析。統計的に検出した急上昇項目一覧とAIサマリーを永続化する新規テーブル。
CREATE TABLE trend_reports (
    id                      UUID PRIMARY KEY,
    platform                VARCHAR(30),
    recent_window_days      INTEGER NOT NULL,
    baseline_window_days    INTEGER NOT NULL,
    items                   TEXT NOT NULL,
    ai_summary              TEXT,
    created_at              TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_trend_reports_platform_created_at ON trend_reports (platform, created_at DESC);
