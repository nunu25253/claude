# 09. DB設計詳細

本ドキュメントは `05_er_diagram.md` のER図に対応するテーブル定義詳細です。DBMSはPostgreSQL 16を前提とします。主キーはすべて `UUID`（`gen_random_uuid()` を既定値とする。`pgcrypto` または PostgreSQL 13+ の `gen_random_uuid()` を利用）とし、外部公開されるIDの推測を防ぎます。

## 共通ルール

- すべてのテーブルに `created_at TIMESTAMPTZ NOT NULL DEFAULT now()` を付与する（一部、更新のないマスタ的テーブルは `updated_at` を省略）。
- 更新が発生するテーブルには `updated_at TIMESTAMPTZ NOT NULL DEFAULT now()` を付与し、アプリケーション側またはトリガーで更新時に自動更新する。
- 論理削除は原則行わず、監査要件があるテーブル（`users` 等）は `is_active` 等のフラグで無効化する。
- 金額・スコア等の小数は `NUMERIC` 型を用い、浮動小数点誤差を避ける。
- 列挙的な値（`post_type`、`role` 等）はアプリケーション側でEnum管理しつつ、DBは `VARCHAR` + `CHECK` 制約で防御する。

## 1. users（ユーザー）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | ユーザーID |
| email | VARCHAR(255) | NOT NULL, UNIQUE | メールアドレス（ログインID） |
| password_hash | VARCHAR(255) | NOT NULL | パスワードハッシュ（bcrypt等） |
| name | VARCHAR(100) | NOT NULL | 表示名 |
| role | VARCHAR(20) | NOT NULL, DEFAULT 'USER', CHECK (role IN ('USER','ADMIN')) | ロール |
| plan | VARCHAR(20) | NOT NULL, DEFAULT 'FREE', CHECK (plan IN ('FREE','PRO','ENTERPRISE')) | 契約プラン |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 有効フラグ |
| last_login_at | TIMESTAMPTZ | NULL | 最終ログイン日時 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 作成日時 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 更新日時 |

**インデックス**: `UNIQUE INDEX ux_users_email (email)`

## 2. user_settings（ユーザー設定）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | 設定ID |
| user_id | UUID | NOT NULL, UNIQUE, FK → users(id) ON DELETE CASCADE | 対象ユーザー |
| notification_email_enabled | BOOLEAN | NOT NULL, DEFAULT true | メール通知有効フラグ |
| default_platform_id | UUID | NULL, FK → platforms(id) ON DELETE SET NULL | デフォルト表示SNS |
| locale | VARCHAR(10) | NOT NULL, DEFAULT 'ja' | 表示言語 |
| timezone | VARCHAR(50) | NOT NULL, DEFAULT 'Asia/Tokyo' | タイムゾーン |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 作成日時 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 更新日時 |

**インデックス**: `UNIQUE INDEX ux_user_settings_user_id (user_id)`

## 3. platforms（プラットフォームマスタ）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | プラットフォームID |
| code | VARCHAR(20) | NOT NULL, UNIQUE, CHECK (code IN ('INSTAGRAM','TIKTOK','X','YOUTUBE','PINTEREST','THREADS')) | プラットフォームコード |
| name | VARCHAR(50) | NOT NULL | 表示名 |
| api_provider | VARCHAR(100) | NULL | 利用する公式API名（例: Instagram Graph API） |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | 提供中フラグ（将来SNS追加時はfalseで先行登録可） |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 作成日時 |

**インデックス**: `UNIQUE INDEX ux_platforms_code (code)`

## 4. social_accounts（分析対象SNSアカウント：自社・競合）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | アカウントID |
| platform_id | UUID | NOT NULL, FK → platforms(id) | 所属プラットフォーム |
| platform_account_id | VARCHAR(150) | NOT NULL | SNS側のアカウントID |
| username | VARCHAR(150) | NOT NULL | ユーザー名（@handle） |
| display_name | VARCHAR(200) | NULL | 表示名 |
| profile_url | VARCHAR(500) | NOT NULL | プロフィールURL |
| avatar_url | VARCHAR(500) | NULL | アイコン画像URL |
| follower_count | BIGINT | NULL | フォロワー数（公開されている場合のみ） |
| genre | VARCHAR(50) | NULL | ジャンル/カテゴリ |
| tracking_enabled | BOOLEAN | NOT NULL, DEFAULT true | 追跡対象フラグ（定期データ取得バッチの対象とするか。実装ではV3マイグレーションで追加） |
| first_tracked_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 初回登録日時 |
| last_synced_at | TIMESTAMPTZ | NULL | 最終データ同期日時 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 作成日時 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 更新日時 |

**インデックス**:
- `UNIQUE INDEX ux_social_accounts_platform_account (platform_id, platform_account_id)`
- `INDEX ix_social_accounts_username (username)`
- `INDEX ix_social_accounts_genre (genre)`

## 5. posts（投稿）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | 投稿ID |
| social_account_id | UUID | NOT NULL, FK → social_accounts(id) ON DELETE CASCADE | 投稿者アカウント |
| platform_id | UUID | NOT NULL, FK → platforms(id) | プラットフォーム（非正規化。検索高速化目的） |
| platform_post_id | VARCHAR(150) | NOT NULL | SNS側の投稿ID |
| post_url | VARCHAR(500) | NOT NULL | 投稿URL |
| post_type | VARCHAR(20) | NOT NULL, CHECK (post_type IN ('REEL','IMAGE','VIDEO','CAROUSEL','TEXT')) | 投稿種別 |
| caption | TEXT | NULL | キャプション本文 |
| posted_at | TIMESTAMPTZ | NOT NULL | 投稿日時 |
| video_duration_sec | INTEGER | NULL, CHECK (video_duration_sec IS NULL OR video_duration_sec >= 0) | 動画時間（秒） |
| image_count | INTEGER | NULL, CHECK (image_count IS NULL OR image_count >= 0) | 画像枚数（カルーセル等） |
| like_count | BIGINT | NOT NULL, DEFAULT 0 | いいね数（最新値キャッシュ） |
| comment_count | BIGINT | NOT NULL, DEFAULT 0 | コメント数（最新値キャッシュ） |
| view_count | BIGINT | NULL | 再生数（公開されている場合） |
| share_count | BIGINT | NULL | シェア数（取得可能な場合） |
| engagement_rate | NUMERIC(6,3) | NULL | エンゲージメント率（計算キャッシュ） |
| raw_metadata | JSONB | NULL | 公式APIレスポンス原本（公開メタデータ） |
| fetched_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 最終取得日時 |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 作成日時 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 更新日時 |

**インデックス**:
- `UNIQUE INDEX ux_posts_platform_post (platform_id, platform_post_id)`
- `INDEX ix_posts_social_account_id (social_account_id)`
- `INDEX ix_posts_posted_at (posted_at DESC)`
- `INDEX ix_posts_post_type (post_type)`
- `INDEX ix_posts_like_count (like_count DESC)`
- `GIN INDEX ix_posts_caption_fts ON posts USING GIN (to_tsvector('simple', coalesce(caption, '')))`（キーワード検索用全文検索インデックス）

## 6. hashtags（ハッシュタグマスタ）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | ハッシュタグID |
| name | VARCHAR(100) | NOT NULL | 表示用タグ文字列（大文字小文字・全角半角を保持） |
| normalized_name | VARCHAR(100) | NOT NULL, UNIQUE | 正規化済みタグ（検索・重複排除用、小文字統一） |
| usage_count | BIGINT | NOT NULL, DEFAULT 0 | 使用回数（集計キャッシュ） |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 作成日時 |

**インデックス**: `UNIQUE INDEX ux_hashtags_normalized_name (normalized_name)`

## 7. post_hashtags（投稿×ハッシュタグ 中間テーブル）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| post_id | UUID | NOT NULL, FK → posts(id) ON DELETE CASCADE | 投稿ID |
| hashtag_id | UUID | NOT NULL, FK → hashtags(id) ON DELETE CASCADE | ハッシュタグID |
| position | INTEGER | NULL | キャプション内での出現順（任意） |

**主キー**: `PRIMARY KEY (post_id, hashtag_id)`
**インデックス**: `INDEX ix_post_hashtags_hashtag_id (hashtag_id)`

## 8. post_metrics（投稿メトリクス時系列スナップショット）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | メトリクスID |
| post_id | UUID | NOT NULL, FK → posts(id) ON DELETE CASCADE | 対象投稿 |
| captured_at | TIMESTAMPTZ | NOT NULL | 取得時点 |
| like_count | BIGINT | NOT NULL | いいね数（取得時点） |
| comment_count | BIGINT | NOT NULL | コメント数（取得時点） |
| view_count | BIGINT | NULL | 再生数（取得時点、公開されている場合） |
| share_count | BIGINT | NULL | シェア数（取得時点、取得可能な場合） |
| engagement_rate | NUMERIC(6,3) | NULL | エンゲージメント率（取得時点） |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | レコード作成日時 |

**インデックス**:
- `INDEX ix_post_metrics_post_id_captured_at (post_id, captured_at DESC)`（急上昇度合いの算出・推移グラフ表示に使用）

## 9. analysis_results（AI分析結果）

> **注記**: 本節は初期設計時点の理想化されたスキーマであり、実装（`backend/src/main/resources/db/migration/V1__init_schema.sql`, `V6__analysis_result_phase5_fields.sql`）とは列名・型（JSONB vs TEXT等）が異なる。実装の正確なスキーマは `docs/phases/phase5_post_analysis.md` を参照。Phase5では `genre`/`sub_genre`/`post_purpose`/`post_structure_analysis`/`strengths`/`weaknesses`（すべてTEXT系）を実装に追加済み。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | 分析結果ID |
| post_id | UUID | NOT NULL, FK → posts(id) ON DELETE CASCADE | 対象投稿 |
| requested_by_user_id | UUID | NULL, FK → users(id) ON DELETE SET NULL | 分析を要求したユーザー（システム自動分析の場合NULL） |
| ai_model | VARCHAR(50) | NOT NULL | 使用したAIモデル名 |
| summary | TEXT | NULL | バズった理由の要約 |
| target_audience | TEXT | NULL | ターゲット層分析 |
| hook_analysis | TEXT | NULL | フック分析 |
| cta_analysis | TEXT | NULL | CTA分析 |
| sentiment_analysis | JSONB | NULL | 感情分析結果 |
| video_structure_analysis | JSONB | NULL | 動画構成分析（VIDEO/REEL向け） |
| carousel_structure_analysis | JSONB | NULL | カルーセル構成分析（CAROUSEL向け） |
| title_analysis | TEXT | NULL | タイトル分析 |
| text_analysis | TEXT | NULL | 文章分析 |
| posting_time_analysis | TEXT | NULL | 投稿時間分析 |
| hashtag_analysis | JSONB | NULL | ハッシュタグ分析 |
| improvement_suggestions | JSONB | NULL | 改善提案リスト |
| similar_posts | JSONB | NULL | 類似投稿ID・類似理由のリスト |
| raw_ai_response | JSONB | NULL | AI応答の原本（デバッグ・再解析用） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING', CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED')) | 処理ステータス |
| created_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 作成日時 |
| updated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 更新日時 |

**インデックス**:
- `INDEX ix_analysis_results_post_id (post_id)`
- `INDEX ix_analysis_results_requested_by_user_id (requested_by_user_id)`
- `INDEX ix_analysis_results_status (status)`

## 10. buzz_scores（BuzzScore算出結果）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | スコアID |
| post_id | UUID | NOT NULL, FK → posts(id) ON DELETE CASCADE | 対象投稿 |
| analysis_result_id | UUID | NOT NULL, FK → analysis_results(id) ON DELETE CASCADE | 元になった分析結果 |
| total_score | NUMERIC(5,2) | NOT NULL, CHECK (total_score >= 0 AND total_score <= 100) | 総合BuzzScore（0-100） |
| engagement_score | NUMERIC(5,2) | NULL | エンゲージメント率スコア |
| view_score | NUMERIC(5,2) | NULL | 再生数スコア |
| comment_rate_score | NUMERIC(5,2) | NULL | コメント率スコア |
| format_score | NUMERIC(5,2) | NULL | 投稿形式スコア |
| hashtag_score | NUMERIC(5,2) | NULL | ハッシュタグスコア |
| posting_time_score | NUMERIC(5,2) | NULL | 投稿時間スコア |
| text_structure_score | NUMERIC(5,2) | NULL | 文章構成スコア |
| ai_insight_score | NUMERIC(5,2) | NULL | AI分析結果スコア |
| strategy_version | VARCHAR(20) | NOT NULL | 採点基準バージョン |
| calculated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 算出日時 |

**インデックス**:
- `UNIQUE INDEX ux_buzz_scores_post_strategy_version (post_id, strategy_version)`
- `INDEX ix_buzz_scores_total_score (total_score DESC)`（ランキング生成に使用）

## 11. competitor_stats（競合統計）

> **注記**: 実装（`V1__init_schema.sql` + `V7__competitor_stats_phase9_fields.sql`）では、本節の理想化スキーマとカラム構成が異なる。実装では `average_view_count`(DOUBLE PRECISION, NULL許容)、`post_format_distribution`/`genre_distribution`（いずれもJSON文字列としてTEXT列に格納、形式/ジャンルごとの割合を保持するMap。本節の`dominant_post_type`/`dominant_genre`のような単一値ではなく分布そのものを保持する）を追加している。詳細は `docs/phases/phase9_competitor_analysis.md` を参照。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | 統計ID |
| social_account_id | UUID | NOT NULL, FK → social_accounts(id) ON DELETE CASCADE | 対象アカウント |
| period_type | VARCHAR(20) | NOT NULL, CHECK (period_type IN ('WEEKLY','MONTHLY','ALL_TIME')) | 集計期間種別 |
| period_start | DATE | NOT NULL | 集計期間開始日 |
| period_end | DATE | NOT NULL | 集計期間終了日 |
| post_count | INTEGER | NOT NULL, DEFAULT 0 | 集計対象投稿数 |
| avg_like_count | NUMERIC(12,2) | NULL | 平均いいね数 |
| avg_comment_count | NUMERIC(12,2) | NULL | 平均コメント数 |
| avg_view_count | NUMERIC(12,2) | NULL | 平均再生数 |
| posting_frequency_per_week | NUMERIC(6,2) | NULL | 週あたり投稿頻度 |
| avg_video_duration_sec | NUMERIC(8,2) | NULL | 平均動画時間（秒） |
| avg_caption_length | NUMERIC(8,2) | NULL | 平均文字数 |
| dominant_post_type | VARCHAR(20) | NULL | 主要投稿種別 |
| dominant_genre | VARCHAR(50) | NULL | 主要ジャンル |
| top_posting_hour | SMALLINT | NULL, CHECK (top_posting_hour IS NULL OR (top_posting_hour >= 0 AND top_posting_hour <= 23)) | 最頻投稿時間帯（0-23時） |
| computed_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 集計日時 |

**インデックス**:
- `UNIQUE INDEX ux_competitor_stats_account_period (social_account_id, period_type, period_start)`
- `INDEX ix_competitor_stats_period (period_type, period_start)`

## 12. rankings（ランキング実行スナップショット）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | ランキングID |
| ranking_type | VARCHAR(20) | NOT NULL, CHECK (ranking_type IN ('TRENDING','WEEKLY','MONTHLY','GENRE','PLATFORM')) | ランキング種別（急上昇/週間/月間/ジャンル別/SNS別） |
| platform_id | UUID | NULL, FK → platforms(id) ON DELETE CASCADE | 対象プラットフォーム（SNS別ランキングで使用、それ以外はNULL=全SNS横断） |
| genre | VARCHAR(50) | NULL | 対象ジャンル（ジャンル別ランキングで使用） |
| period_start | TIMESTAMPTZ | NOT NULL | 集計対象期間開始 |
| period_end | TIMESTAMPTZ | NOT NULL | 集計対象期間終了 |
| generated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 生成日時 |

**インデックス**:
- `INDEX ix_rankings_type_generated_at (ranking_type, generated_at DESC)`
- `INDEX ix_rankings_platform_id (platform_id)`

## 13. ranking_entries（ランキング明細）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | 明細ID |
| ranking_id | UUID | NOT NULL, FK → rankings(id) ON DELETE CASCADE | 所属ランキング |
| post_id | UUID | NOT NULL, FK → posts(id) ON DELETE CASCADE | 対象投稿 |
| rank_position | INTEGER | NOT NULL, CHECK (rank_position > 0) | 順位 |
| score | NUMERIC(10,2) | NOT NULL | 順位付けに用いたスコア（BuzzScoreまたは成長率） |

**インデックス**:
- `UNIQUE INDEX ux_ranking_entries_ranking_post (ranking_id, post_id)`
- `UNIQUE INDEX ux_ranking_entries_ranking_rank (ranking_id, rank_position)`

## 14. reports（AIレポート）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | レポートID |
| user_id | UUID | NOT NULL, FK → users(id) ON DELETE CASCADE | 生成したユーザー |
| analysis_result_id | UUID | NOT NULL, FK → analysis_results(id) ON DELETE CASCADE | 元になった分析結果 |
| title | VARCHAR(200) | NOT NULL | レポートタイトル |
| format | VARCHAR(10) | NOT NULL, CHECK (format IN ('PDF','MARKDOWN','HTML')) | 出力形式 |
| file_url | VARCHAR(500) | NULL | 保存先URL（S3互換ストレージ） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING', CHECK (status IN ('PENDING','PROCESSING','COMPLETED','FAILED')) | 生成ステータス |
| generated_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 生成日時 |

**インデックス**:
- `INDEX ix_reports_user_id (user_id)`
- `INDEX ix_reports_analysis_result_id (analysis_result_id)`

## 15. saved_analyses（保存済み分析）

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK, DEFAULT gen_random_uuid() | 保存ID |
| user_id | UUID | NOT NULL, FK → users(id) ON DELETE CASCADE | 保存したユーザー |
| analysis_result_id | UUID | NOT NULL, FK → analysis_results(id) ON DELETE CASCADE | 保存対象の分析結果 |
| folder_name | VARCHAR(100) | NULL | フォルダ名 |
| memo | TEXT | NULL | メモ |
| saved_at | TIMESTAMPTZ | NOT NULL, DEFAULT now() | 保存日時 |

**インデックス**:
- `UNIQUE INDEX ux_saved_analyses_user_analysis (user_id, analysis_result_id)`
- `INDEX ix_saved_analyses_user_folder (user_id, folder_name)`

## 16. embeddings（Embeddingベクトル。AIマーケティングOS Phase3で追加）

実装は `backend/src/main/resources/db/migration/V4__pgvector_embeddings.sql`。`pgvector` 拡張が必要（`docker-compose.yml` は `pgvector/pgvector:pg16` イメージを使用）。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK | Embedding ID |
| post_id | UUID | NOT NULL, FK → posts(id) ON DELETE CASCADE | 対象投稿 |
| target | VARCHAR(30) | NOT NULL | TITLE / BODY / HASHTAGS / COMMENT_SUMMARY（現状BODY/HASHTAGSのみ生成。理由は `docs/phases/phase3_embeddings.md` 参照） |
| vector | vector(1536) | NOT NULL | Embeddingベクトル（text-embedding-3-small想定） |
| source_text | TEXT | NOT NULL | 生成元テキスト（再生成要否の判定に使用） |
| model | VARCHAR(100) | NOT NULL | 使用モデル名 |
| dimensions | INTEGER | NOT NULL | ベクトル次元数 |
| generated_at | TIMESTAMPTZ | NOT NULL | 生成日時 |

**インデックス**:
- `UNIQUE INDEX uq_embeddings_post_target (post_id, target)`
- `INDEX idx_embeddings_post_id (post_id)`
- 類似検索用のANN(ivfflat/hnsw)インデックスはPhase4（意味検索エンジン）で実データ蓄積後に追加予定（意図的に先送り）

## 17. content_proposals（AI生成投稿企画。AIマーケティングOS Phase10で追加）

実装は `backend/src/main/resources/db/migration/V8__content_proposals.sql`。Phase8（共通点分析）の結果を元にAIが生成した投稿企画を、1回の生成リクエスト単位（`generation_id`）でグルーピングして保持する新規テーブル（既存テーブルの拡張ではなく新設。理由は `docs/phases/phase10_proposal_generation.md` 参照）。特定の投稿・アカウントに紐付くものではないためFK制約は持たない。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK | 企画ID |
| generation_id | UUID | NOT NULL | 生成単位のグルーピングID（1回の生成で既定20件が同一値） |
| sequence_number | INTEGER | NOT NULL | 生成単位内の通し番号（1始まり） |
| title | TEXT | NOT NULL | 投稿タイトル案 |
| hook_pattern | TEXT | NULL許容 | 冒頭フック案 |
| structure_summary | TEXT | NULL許容 | 投稿構成案 |
| call_to_action | TEXT | NULL許容 | CTA案 |
| target_audience | TEXT | NULL許容 | 想定ターゲット |
| genre | VARCHAR(100) | NULL許容 | ジャンル |
| recommended_format | VARCHAR(30) | NULL許容 | 推奨コンテンツ形式（`ContentFormat`。AI応答が未知の値の場合はNULL） |
| reasoning | TEXT | NULL許容 | この企画が有効と考える理由 |
| created_at | TIMESTAMPTZ | NOT NULL | 生成日時 |

**インデックス**:
- `INDEX idx_content_proposals_generation_id (generation_id)`

## 18. video_scripts（AI生成動画台本。AIマーケティングOS Phase11で追加）

実装は `backend/src/main/resources/db/migration/V9__video_scripts.sql`。Phase10の`content_proposals`1件から、尺(30/60/90秒)ごとに複数生成できる動画台本を保持する。カット構成（`cuts`）は可変長のため、Phase9の分布Mapと同様の方針でJSON文字列としてTEXT列に格納する（`ScriptCutListJsonConverter`）。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK | 台本ID |
| proposal_id | UUID | NOT NULL, FK → content_proposals(id) ON DELETE CASCADE | 元になった投稿企画 |
| duration_seconds | INTEGER | NOT NULL | 尺（秒）。アプリ層で30/60/90のみに制限 |
| bgm_image | TEXT | NULL許容 | BGMの雰囲気イメージ |
| call_to_action | TEXT | NULL許容 | 動画全体のCTA |
| cuts | TEXT | NOT NULL | カット構成（JSON配列。cutNumber/startSecond/endSecond/narration/telop/visualDirection） |
| created_at | TIMESTAMPTZ | NOT NULL | 生成日時 |

**インデックス**:
- `INDEX idx_video_scripts_proposal_id (proposal_id)`

## 19. carousels（AI生成カルーセル。AIマーケティングOS Phase12で追加）

実装は `backend/src/main/resources/db/migration/V10__carousels.sql`。Phase10の`content_proposals`1件から生成できるInstagramカルーセル（2〜8ページ）を保持する。ページ構成（`pages`）はPhase11の`cuts`と同方針でJSON文字列としてTEXT列に格納する（`CarouselPageListJsonConverter`）。各ページの役割（HOOK/EXPLANATION/CTA）はAIに判定させず、application層がページ配列内の位置（先頭=HOOK、末尾=CTA、それ以外=EXPLANATION）から機械的に決定した上で保存する。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK | カルーセルID |
| proposal_id | UUID | NOT NULL, FK → content_proposals(id) ON DELETE CASCADE | 元になった投稿企画 |
| pages | TEXT | NOT NULL | ページ構成（JSON配列。pageNumber/role/headline/bodyText/visualDirection） |
| created_at | TIMESTAMPTZ | NOT NULL | 生成日時 |

**インデックス**:
- `INDEX idx_carousels_proposal_id (proposal_id)`

## 20. image_prompt_sets（AI生成画像プロンプト一式。AIマーケティングOS Phase13で追加）

実装は `backend/src/main/resources/db/migration/V11__image_prompt_sets.sql`。Phase11の`video_scripts`またはPhase12の`carousels`いずれか1件から、各要素（カット/ページ）の`visualDirection`を元に生成した画像生成プロンプト（テキストのみ。実際の画像生成は行わない）を保持する。`source_id`は`video_scripts.id`または`carousels.id`を指すポリモーフィックな参照のため、DB外部キー制約は付けない（アプリ層で整合性を保証。詳細は`docs/phases/phase13_image_prompt_generation.md`参照）。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK | プロンプト一式のID |
| source_type | VARCHAR(30) | NOT NULL | `VIDEO_SCRIPT` / `CAROUSEL` |
| source_id | UUID | NOT NULL | 参照先（`video_scripts.id`または`carousels.id`。FK制約なし） |
| prompts | TEXT | NOT NULL | プロンプト一覧（JSON配列。index/originalDirection/generatedPrompt） |
| created_at | TIMESTAMPTZ | NOT NULL | 生成日時 |

**インデックス**:
- `INDEX idx_image_prompt_sets_source (source_type, source_id)`

## 21. content_evaluations（AI投稿評価結果。AIマーケティングOS Phase14で追加）

実装は `backend/src/main/resources/db/migration/V12__content_evaluations.sql`。ユーザーが作成した（またはAIが生成した）投稿内容（台本/カルーセル等）の評価結果を保持する。`proposal_id`は任意（指定して評価した場合のみPhase10の企画との一致率を算出）。企画が削除されても評価履歴自体は残すため`ON DELETE SET NULL`とする（他フェーズの`CASCADE`と異なる方針。理由は`docs/phases/phase14_post_evaluation.md`参照）。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK | 評価ID |
| proposal_id | UUID | NULL許容, FK → content_proposals(id) ON DELETE SET NULL | 比較対象とした元企画（未指定可） |
| title | TEXT | NULL許容 | 評価対象のタイトル |
| match_rate_percent | DOUBLE PRECISION | NULL許容 | 元企画との一致率（proposal_id未指定時はNULL。0%と区別する） |
| target_audience_estimate | TEXT | NULL許容 | AIによる想定ターゲットの推定 |
| improvement_suggestions | TEXT | NOT NULL | 改善提案一覧（JSON配列） |
| hook_improvement | TEXT | NULL許容 | フックの改善案 |
| cta_improvement | TEXT | NULL許容 | CTAの改善案 |
| predicted_score | INTEGER | NOT NULL | 0〜100の予測投稿スコア（AIの定性評価。Phase7のランキングスコアとは別指標） |
| created_at | TIMESTAMPTZ | NOT NULL | 評価日時 |

**インデックス**:
- `INDEX idx_content_evaluations_proposal_id (proposal_id)`

## 22. trend_reports（トレンド分析結果。AIマーケティングOS Phase15で追加）

実装は `backend/src/main/resources/db/migration/V13__trend_reports.sql`。直近ウィンドウ・ベースラインウィンドウの2期間比較で統計的に検出した急上昇項目（ハッシュタグ/ジャンル/コンテンツ形式）と、そのAIサマリーを保持する。`platform`がNULLの場合は全プラットフォーム対象のレポートを表す。

| カラム名 | 型 | 制約 | 説明 |
|----------|----|------|------|
| id | UUID | PK | レポートID |
| platform | VARCHAR(30) | NULL許容 | 対象プラットフォーム（NULLは全プラットフォーム対象） |
| recent_window_days | INTEGER | NOT NULL | 直近ウィンドウの日数 |
| baseline_window_days | INTEGER | NOT NULL | ベースラインウィンドウの日数 |
| items | TEXT | NOT NULL | 検出項目一覧（JSON配列。category/value/recentCount/baselineCount/growthRatePercent/emerging） |
| ai_summary | TEXT | NULL許容 | AIによる自然言語サマリー |
| created_at | TIMESTAMPTZ | NOT NULL | 分析実行日時 |

**インデックス**:
- `INDEX idx_trend_reports_platform_created_at (platform, created_at DESC)`（最新レポート取得用）

## 外部キー制約一覧（サマリー）

| 子テーブル | 列 | 親テーブル | ON DELETE |
|-----------|----|-----------|-----------|
| user_settings | user_id | users(id) | CASCADE |
| user_settings | default_platform_id | platforms(id) | SET NULL |
| social_accounts | platform_id | platforms(id) | RESTRICT |
| posts | social_account_id | social_accounts(id) | CASCADE |
| posts | platform_id | platforms(id) | RESTRICT |
| post_hashtags | post_id | posts(id) | CASCADE |
| post_hashtags | hashtag_id | hashtags(id) | CASCADE |
| post_metrics | post_id | posts(id) | CASCADE |
| analysis_results | post_id | posts(id) | CASCADE |
| embeddings | post_id | posts(id) | CASCADE |
| analysis_results | requested_by_user_id | users(id) | SET NULL |
| buzz_scores | post_id | posts(id) | CASCADE |
| buzz_scores | analysis_result_id | analysis_results(id) | CASCADE |
| competitor_stats | social_account_id | social_accounts(id) | CASCADE |
| rankings | platform_id | platforms(id) | CASCADE |
| ranking_entries | ranking_id | rankings(id) | CASCADE |
| ranking_entries | post_id | posts(id) | CASCADE |
| reports | user_id | users(id) | CASCADE |
| reports | analysis_result_id | analysis_results(id) | CASCADE |
| saved_analyses | user_id | users(id) | CASCADE |
| saved_analyses | analysis_result_id | analysis_results(id) | CASCADE |
| video_scripts | proposal_id | content_proposals(id) | CASCADE |
| carousels | proposal_id | content_proposals(id) | CASCADE |
| content_evaluations | proposal_id | content_proposals(id) | SET NULL |

## インデックス設計方針

- **検索・一覧系（`posts`、`social_accounts`）**：頻繁に絞り込みに使うカラム（`posted_at`、`post_type`、`genre`、`username`）に単独インデックスを付与し、キーワード検索用に `caption` へGIN（全文検索）インデックスを付与する。
- **ランキング・スコア系（`buzz_scores`、`ranking_entries`）**：ソート対象カラム（`total_score`、`rank_position`）に降順インデックスを付与し、上位N件取得を高速化する。
- **時系列系（`post_metrics`）**：`(post_id, captured_at)` の複合インデックスにより、特定投稿の推移取得を高速化する。
- **一意性制約**：SNS側ID（`platform_post_id`、`platform_account_id`）と自社IDのマッピングは複合UNIQUEインデックスで重複取得を防止し、再取得時は `UPSERT`（`ON CONFLICT`）で更新する。

## マイグレーション管理

Flyway（`backend/src/main/resources/db/migration/`）でスキーマをバージョン管理します。`V1__init_schema.sql` で本ドキュメントの全テーブルを作成し、`V2__seed_platforms.sql` で `platforms` テーブルへ初期データ（INSTAGRAM, TIKTOK, X を `is_active=true`、YOUTUBE, PINTEREST, THREADS を `is_active=false` で先行登録）を投入します。
