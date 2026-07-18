-- Phase12: カルーセル生成AI。AIが生成したInstagramカルーセルを永続化する新規テーブル。
-- 1件のcontent_proposalから複数のcarouselを生成できる。
CREATE TABLE carousels (
    id                  UUID PRIMARY KEY,
    proposal_id         UUID NOT NULL REFERENCES content_proposals (id) ON DELETE CASCADE,
    pages               TEXT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_carousels_proposal_id ON carousels (proposal_id);
