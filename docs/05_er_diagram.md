# 05. ER図

本ドキュメントは本システムのデータモデルをER図（Mermaid `erDiagram`）として表現したものです。詳細なカラム定義・型・制約・インデックスは `09_db_design.md` を参照してください。テーブル名・カラム名は全ドキュメントで統一しています。

## 全体ER図

```mermaid
erDiagram
    USERS ||--o{ SOCIAL_ACCOUNTS : "tracks (登録)"
    USERS ||--o{ ANALYSIS_RESULTS : "requests"
    USERS ||--o{ REPORTS : "generates"
    USERS ||--o{ SAVED_ANALYSES : "saves"
    USERS ||--|| USER_SETTINGS : "has"

    PLATFORMS ||--o{ SOCIAL_ACCOUNTS : "belongs to"
    PLATFORMS ||--o{ POSTS : "belongs to"
    PLATFORMS ||--o{ RANKINGS : "scoped by (nullable)"

    SOCIAL_ACCOUNTS ||--o{ POSTS : "publishes"
    SOCIAL_ACCOUNTS ||--o{ COMPETITOR_STATS : "aggregated into"

    POSTS ||--o{ POST_METRICS : "has time-series"
    POSTS ||--o{ POST_HASHTAGS : "tagged with"
    POSTS ||--o{ ANALYSIS_RESULTS : "analyzed into"
    POSTS ||--o{ BUZZ_SCORES : "scored into"
    POSTS ||--o{ RANKING_ENTRIES : "appears in"

    HASHTAGS ||--o{ POST_HASHTAGS : "used in"

    ANALYSIS_RESULTS ||--o{ BUZZ_SCORES : "derives"
    ANALYSIS_RESULTS ||--o{ REPORTS : "exported as"
    ANALYSIS_RESULTS ||--o{ SAVED_ANALYSES : "bookmarked as"

    RANKINGS ||--o{ RANKING_ENTRIES : "contains"

    USERS {
        uuid id PK
        varchar email UK
        varchar password_hash
        varchar name
        varchar role
        varchar plan
        boolean is_active
        timestamptz last_login_at
        timestamptz created_at
        timestamptz updated_at
    }

    USER_SETTINGS {
        uuid id PK
        uuid user_id FK
        boolean notification_email_enabled
        uuid default_platform_id FK
        varchar locale
        varchar timezone
        timestamptz created_at
        timestamptz updated_at
    }

    PLATFORMS {
        uuid id PK
        varchar code UK
        varchar name
        varchar api_provider
        boolean is_active
        timestamptz created_at
    }

    SOCIAL_ACCOUNTS {
        uuid id PK
        uuid platform_id FK
        varchar platform_account_id
        varchar username
        varchar display_name
        varchar profile_url
        varchar avatar_url
        bigint follower_count
        varchar genre
        boolean is_tracked
        timestamptz first_tracked_at
        timestamptz last_synced_at
        timestamptz created_at
        timestamptz updated_at
    }

    POSTS {
        uuid id PK
        uuid social_account_id FK
        uuid platform_id FK
        varchar platform_post_id
        varchar post_url
        varchar post_type
        text caption
        timestamptz posted_at
        int video_duration_sec
        int image_count
        bigint like_count
        bigint comment_count
        bigint view_count
        bigint share_count
        numeric engagement_rate
        jsonb raw_metadata
        timestamptz fetched_at
        timestamptz created_at
        timestamptz updated_at
    }

    HASHTAGS {
        uuid id PK
        varchar name
        varchar normalized_name UK
        bigint usage_count
        timestamptz created_at
    }

    POST_HASHTAGS {
        uuid post_id FK
        uuid hashtag_id FK
        int position
    }

    POST_METRICS {
        uuid id PK
        uuid post_id FK
        timestamptz captured_at
        bigint like_count
        bigint comment_count
        bigint view_count
        bigint share_count
        numeric engagement_rate
        timestamptz created_at
    }

    ANALYSIS_RESULTS {
        uuid id PK
        uuid post_id FK
        uuid requested_by_user_id FK
        varchar ai_model
        text summary
        text target_audience
        text hook_analysis
        text cta_analysis
        jsonb sentiment_analysis
        jsonb video_structure_analysis
        jsonb carousel_structure_analysis
        text title_analysis
        text text_analysis
        text posting_time_analysis
        jsonb hashtag_analysis
        jsonb improvement_suggestions
        jsonb similar_posts
        jsonb raw_ai_response
        varchar status
        timestamptz created_at
        timestamptz updated_at
    }

    BUZZ_SCORES {
        uuid id PK
        uuid post_id FK
        uuid analysis_result_id FK
        numeric total_score
        numeric engagement_score
        numeric view_score
        numeric comment_rate_score
        numeric format_score
        numeric hashtag_score
        numeric posting_time_score
        numeric text_structure_score
        numeric ai_insight_score
        varchar strategy_version
        timestamptz calculated_at
    }

    COMPETITOR_STATS {
        uuid id PK
        uuid social_account_id FK
        varchar period_type
        date period_start
        date period_end
        int post_count
        numeric avg_like_count
        numeric avg_comment_count
        numeric avg_view_count
        numeric posting_frequency_per_week
        numeric avg_video_duration_sec
        numeric avg_caption_length
        varchar dominant_post_type
        varchar dominant_genre
        smallint top_posting_hour
        timestamptz computed_at
    }

    RANKINGS {
        uuid id PK
        varchar ranking_type
        uuid platform_id FK
        varchar genre
        timestamptz period_start
        timestamptz period_end
        timestamptz generated_at
    }

    RANKING_ENTRIES {
        uuid id PK
        uuid ranking_id FK
        uuid post_id FK
        int rank_position
        numeric score
    }

    REPORTS {
        uuid id PK
        uuid user_id FK
        uuid analysis_result_id FK
        varchar title
        varchar format
        varchar file_url
        varchar status
        timestamptz generated_at
    }

    SAVED_ANALYSES {
        uuid id PK
        uuid user_id FK
        uuid analysis_result_id FK
        varchar folder_name
        text memo
        timestamptz saved_at
    }
```

## モデリング上のポイント

- **`platforms` による将来拡張**：SNS種別はコード（enum的な文字列）ではなくマスタテーブル `platforms` として管理することで、YouTube / Pinterest / Threads 追加時にアプリケーションコードの変更を最小限（`SocialPlatform` 実装クラスの追加とマスタレコード追加のみ）にできます。
- **`social_accounts` の汎用化**：自社アカウント・競合アカウントを区別せず同一テーブルで扱い、`is_tracked` フラグで管理対象かどうかを判定します。将来的にアカウント種別（自社/競合/ウォッチリスト）を増やす場合も列挙値の追加のみで対応できます。
- **`posts` と `post_metrics` の分離**：`posts` には最新値（いいね数等）をキャッシュとして保持しつつ、`post_metrics` に取得時点ごとのスナップショットを時系列で保存します。これにより「伸び方（急上昇度合い）」の算出や再取得のたびの履歴管理が可能になります。非公開データ（インプレッション等）はどちらのテーブルにも列を持たせません。
- **`hashtags` / `post_hashtags` の多対多**：1つのハッシュタグが複数投稿で使われ、1つの投稿が複数ハッシュタグを持つ多対多関係を正規化して表現します。
- **`analysis_results` の柔軟なスキーマ**：構成分析・感情分析・改善提案などは投稿種別によって内容の形が変わるため `jsonb` 型で保持し、将来分析項目が増えてもマイグレーションを最小化できます。
- **`buzz_scores` のバージョニング**：`strategy_version` を持たせることで、採点基準（Strategy群の重み付けロジック）を改訂した際に旧スコアとの比較・再計算が可能です。
- **`rankings` / `ranking_entries` の分離**：ランキングという「実行結果のスナップショット」と、その中身である「順位付けされた投稿」を分けることで、急上昇/週間/月間/ジャンル別/SNS別のあらゆる軸のランキングを同一スキーマで表現できます。
- **`reports` と `analysis_results` の関連**：レポートは必ず1つの分析結果に紐づき、PDF/Markdown/HTMLの出力形式ごとに別レコードとして保存します（同じ分析結果から複数形式を出力可能）。
