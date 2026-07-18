-- Phase10: 企画生成AI。AIが生成した投稿企画を永続化する新規テーブル。
-- 1回の生成(既定20件)は同一generation_idでグルーピングされる。
CREATE TABLE content_proposals (
    id                  UUID PRIMARY KEY,
    generation_id       UUID NOT NULL,
    sequence_number     INTEGER NOT NULL,
    title               TEXT NOT NULL,
    hook_pattern        TEXT,
    structure_summary   TEXT,
    call_to_action      TEXT,
    target_audience     TEXT,
    genre               VARCHAR(100),
    recommended_format  VARCHAR(30),
    reasoning           TEXT,
    created_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_content_proposals_generation_id ON content_proposals (generation_id);
