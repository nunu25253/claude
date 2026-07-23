-- 再分析のたびにbuzz_scoresが上書きされ時系列比較ができない問題を解消するための履歴テーブル。
-- buzz_scoresは引き続き「最新値」として既存の参照箇所(ランキング等)にそのまま使う。
CREATE TABLE buzz_score_history (
    id             UUID PRIMARY KEY,
    post_id        UUID NOT NULL REFERENCES posts (id),
    total_score    DOUBLE PRECISION NOT NULL,
    breakdown      TEXT NOT NULL,
    calculated_at  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_buzz_score_history_post_id_calculated_at
    ON buzz_score_history (post_id, calculated_at);
