-- メール未確認ユーザーでも投稿分析を1回だけ無料体験できるようにするための状態列。
-- NULLは未使用(=まだ体験分析を行っていない)、値ありは使用済みの日時を表す。
-- レビューで指摘された「登録直後のメール確認の壁が初回体験を阻害する」UX課題への対応。

ALTER TABLE users ADD COLUMN trial_analysis_used_at TIMESTAMPTZ;
