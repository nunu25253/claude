-- Phase4: 意味検索エンジン用のANN(近似最近傍)インデックス。
-- IVFFlatは事前学習(クラスタリング)が必要でデータ量が少ないと精度が不安定になるため、
-- データ量に対して劣化しにくいHNSWを採用する(pgvector 0.5.0以降が必要。pgvector/pgvector:pg16は対応済み)。
-- 現状の検索対象は target='BODY' のみ(docs/phases/phase4_semantic_search.md参照)のため部分インデックスにする。
CREATE INDEX idx_embeddings_body_vector_hnsw
    ON embeddings USING hnsw (vector vector_cosine_ops)
    WHERE target = 'BODY';
