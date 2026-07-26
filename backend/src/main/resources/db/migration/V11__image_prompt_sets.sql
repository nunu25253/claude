-- Phase13: 画像生成プロンプト。台本(video_scripts)/カルーセル(carousels)いずれかを参照する
-- ポリモーフィックな生成物のため、source_idにDB外部キー制約は付けない(アプリ層で整合性を保証)。
CREATE TABLE image_prompt_sets (
    id              UUID PRIMARY KEY,
    source_type     VARCHAR(30) NOT NULL,
    source_id       UUID NOT NULL,
    prompts         TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_image_prompt_sets_source ON image_prompt_sets (source_type, source_id);
