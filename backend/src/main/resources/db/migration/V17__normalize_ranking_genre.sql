-- V2のサンプルデータは rankings.genre を英小文字(例: 'entertainment')で登録していたが、
-- アプリケーションはGenreNormalizerによりフロントエンドの英大文字コード(例: 'ENTERTAINMENT')で
-- 統一して比較・保存するようになった。既存行を同じ表記へ揃える(technology表記のみFRONTENDの
-- 'TECH'と異なるため個別に変換する)。
UPDATE rankings SET genre = 'TECH' WHERE genre = 'technology';
UPDATE rankings SET genre = UPPER(genre) WHERE genre IS NOT NULL AND genre <> UPPER(genre);
