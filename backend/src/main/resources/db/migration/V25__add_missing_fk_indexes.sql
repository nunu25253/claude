-- saved_analyses.post_id は外部キー(REFERENCES posts)だがINDEXが無かった。PostgreSQLは
-- 参照先(posts)行の削除/更新時に参照元テーブルを整合性チェックのため走査するが、参照元に
-- INDEXが無いとシーケンシャルスキャンになりロック保持時間が伸びる(FK列には常にINDEXを
-- 張るのが定石)。現時点でsaved_analyses.post_id自体を条件に検索するクエリは無いが、
-- 将来的な「投稿ごとの保存済み分析検索」にも備えて追加する(レビューで発覚)。
-- なお reports.post_id は idx_reports_post_id (V1__init_schema.sql) で既に対応済み
-- (レビュー時の初期指摘は誤りだったため、ここでは追加しない)。

CREATE INDEX idx_saved_analyses_post_id ON saved_analyses (post_id);
