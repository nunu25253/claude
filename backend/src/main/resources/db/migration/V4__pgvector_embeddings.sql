-- Phase3: Embedding生成（OpenAI Embeddings API + pgvector）
-- 前提: postgresイメージは pgvector/pgvector:pg16（拡張同梱）を使用すること（docker-compose.yml参照）。
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE embeddings (
    id            UUID PRIMARY KEY,
    post_id       UUID NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    target        VARCHAR(30) NOT NULL,        -- TITLE / BODY / HASHTAGS / COMMENT_SUMMARY (現状BODY/HASHTAGSのみ生成)
    vector        vector(1536) NOT NULL,       -- text-embedding-3-small の出力次元数
    source_text   TEXT NOT NULL,               -- 生成元テキスト（再生成要否の判定に使用）
    model         VARCHAR(100) NOT NULL,
    dimensions    INTEGER NOT NULL,
    generated_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_embeddings_post_target UNIQUE (post_id, target)
);

CREATE INDEX idx_embeddings_post_id ON embeddings (post_id);

-- 類似検索用のANN(ivfflat/hnsw)インデックスは、実データが十分に蓄積されるPhase4（意味検索エンジン）で
-- 追加する。データ量が少ない開発初期にivfflatインデックスを作成すると学習効率が悪く、
-- かえって非効率になるため意図的に先送りしている。
