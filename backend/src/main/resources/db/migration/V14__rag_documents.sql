-- Phase16: RAG。既存のembeddings(Phase3、投稿専用)とは別に、多様なテキスト
-- (分析結果/評価/トレンドサマリー等)を索引化する新規テーブル。source_idはポリモーフィックな
-- 参照のためDB外部キー制約は付けない(Phase13のimage_prompt_setsと同方針)。
CREATE TABLE rag_documents (
    id             UUID PRIMARY KEY,
    source_type    VARCHAR(30) NOT NULL,
    source_id      UUID,
    content_text   TEXT NOT NULL,
    vector         vector(1536) NOT NULL,
    model          VARCHAR(100) NOT NULL,
    dimensions     INTEGER NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL
);

-- Phase4のHNSW採用方針を踏襲(pgvector 0.5.0以降が必要。pgvector/pgvector:pg16は対応済み)。
CREATE INDEX idx_rag_documents_vector_hnsw ON rag_documents USING hnsw (vector vector_cosine_ops);
