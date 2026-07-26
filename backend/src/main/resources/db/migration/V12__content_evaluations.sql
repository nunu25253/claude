-- Phase14: 投稿評価AI。ユーザーが作成した投稿内容の評価結果を永続化する新規テーブル。
-- proposal_idは任意(企画との比較評価をした場合のみ設定)。企画削除時も評価履歴は残すためSET NULL。
CREATE TABLE content_evaluations (
    id                          UUID PRIMARY KEY,
    proposal_id                 UUID REFERENCES content_proposals (id) ON DELETE SET NULL,
    title                       TEXT,
    match_rate_percent          DOUBLE PRECISION,
    target_audience_estimate    TEXT,
    improvement_suggestions     TEXT NOT NULL,
    hook_improvement            TEXT,
    cta_improvement             TEXT,
    predicted_score             INTEGER NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_content_evaluations_proposal_id ON content_evaluations (proposal_id);
