-- Phase11: 台本生成AI。AIが生成した動画台本を永続化する新規テーブル。
-- 1件のcontent_proposalから、尺(30/60/90秒)ごとに複数のvideo_scriptを生成できる。
CREATE TABLE video_scripts (
    id                  UUID PRIMARY KEY,
    proposal_id         UUID NOT NULL REFERENCES content_proposals (id) ON DELETE CASCADE,
    duration_seconds    INTEGER NOT NULL,
    bgm_image           TEXT,
    call_to_action      TEXT,
    cuts                TEXT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_video_scripts_proposal_id ON video_scripts (proposal_id);
