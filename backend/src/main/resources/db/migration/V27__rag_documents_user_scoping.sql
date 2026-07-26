-- RAG索引(rag_documents)にuser_idが無く、findNearestが全ユーザーを横断して検索する設計になっていた。
-- これはユーザーAの分析結果がユーザーBへのRAG回答の根拠として漏洩しうる重大なマルチテナント不備であり、
-- RAGアシスタント機能を実際にフロントエンドへ公開する前に修正する(戦略監査レポートで発覚)。
-- 既存の索引ドキュメントは誰が索引登録したか遡って特定できないため、所有者不明なデータとして
-- 削除してから(=このテーブルが本来満たすべきマルチテナント制約を満たさないデータは保持しない)
-- NOT NULL制約を付与する。
ALTER TABLE rag_documents
    ADD COLUMN user_id UUID REFERENCES users (id) ON DELETE CASCADE;

DELETE FROM rag_documents WHERE user_id IS NULL;

ALTER TABLE rag_documents
    ALTER COLUMN user_id SET NOT NULL;

CREATE INDEX idx_rag_documents_user_id ON rag_documents (user_id);
