-- SNS AIバズ分析プラットフォーム: 初期スキーマ
-- UUIDはアプリケーション側(java.util.UUID)で生成するため、DB側のデフォルト値は付与しない。

CREATE TABLE users (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100),
    role            VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE social_accounts (
    id                      UUID PRIMARY KEY,
    platform                VARCHAR(20) NOT NULL,
    external_account_id     VARCHAR(255) NOT NULL,
    username                VARCHAR(255) NOT NULL,
    display_name            VARCHAR(255),
    profile_url             VARCHAR(2048),
    follower_count          BIGINT,
    post_count              BIGINT,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_social_accounts_platform_external_id UNIQUE (platform, external_account_id)
);

CREATE INDEX idx_social_accounts_platform_username ON social_accounts (platform, username);

CREATE TABLE posts (
    id                          UUID PRIMARY KEY,
    social_account_id          UUID NOT NULL REFERENCES social_accounts (id),
    platform                   VARCHAR(20) NOT NULL,
    external_id                VARCHAR(255),
    url                        VARCHAR(2048) NOT NULL,
    published_at                TIMESTAMPTZ,
    author_name                VARCHAR(255),
    caption                    TEXT,
    like_count                 BIGINT,
    comment_count              BIGINT,
    view_count                 BIGINT,
    share_count                BIGINT,
    video_duration_seconds     INTEGER,
    image_count                INTEGER,
    post_type                  VARCHAR(20),
    created_at                 TIMESTAMPTZ NOT NULL,
    updated_at                 TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_posts_platform_external_id UNIQUE (platform, external_id)
);

CREATE INDEX idx_posts_social_account_id ON posts (social_account_id);
CREATE INDEX idx_posts_platform ON posts (platform);
CREATE INDEX idx_posts_published_at ON posts (published_at);
CREATE INDEX idx_posts_like_count ON posts (like_count);

CREATE TABLE post_hashtags (
    post_id     UUID NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    hashtag     VARCHAR(255) NOT NULL
);

CREATE INDEX idx_post_hashtags_hashtag ON post_hashtags (hashtag);
CREATE INDEX idx_post_hashtags_post_id ON post_hashtags (post_id);

CREATE TABLE analysis_results (
    id                              UUID PRIMARY KEY,
    post_id                         UUID NOT NULL REFERENCES posts (id),
    why_it_went_viral               TEXT,
    target_audience                 TEXT,
    hook                            TEXT,
    call_to_action                  TEXT,
    sentiment_analysis              TEXT,
    video_structure_analysis        TEXT,
    carousel_structure_analysis     TEXT,
    title_analysis                  TEXT,
    text_analysis                   TEXT,
    posting_time_analysis           TEXT,
    hashtag_analysis                TEXT,
    improvement_suggestions         TEXT,
    created_at                      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_analysis_results_post_id UNIQUE (post_id)
);

CREATE TABLE buzz_scores (
    id              UUID PRIMARY KEY,
    post_id         UUID NOT NULL REFERENCES posts (id),
    total_score     DOUBLE PRECISION NOT NULL,
    breakdown       TEXT NOT NULL,
    calculated_at   TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_buzz_scores_post_id UNIQUE (post_id)
);

CREATE TABLE competitor_stats (
    id                              UUID PRIMARY KEY,
    social_account_id               UUID NOT NULL REFERENCES social_accounts (id),
    average_like_count              DOUBLE PRECISION NOT NULL,
    average_comment_count           DOUBLE PRECISION NOT NULL,
    posting_frequency_per_week      DOUBLE PRECISION NOT NULL,
    posting_time_distribution       TEXT,
    average_video_duration_seconds  DOUBLE PRECISION,
    average_caption_length          DOUBLE PRECISION NOT NULL,
    top_performing_post_ids         TEXT,
    calculated_at                   TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_competitor_stats_social_account_id UNIQUE (social_account_id)
);

CREATE TABLE reports (
    id                      UUID PRIMARY KEY,
    post_id                 UUID NOT NULL REFERENCES posts (id),
    format                  VARCHAR(20) NOT NULL,
    title                   VARCHAR(500),
    storage_key             VARCHAR(1024) NOT NULL,
    content_size_bytes      BIGINT NOT NULL,
    generated_at            TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_reports_post_id ON reports (post_id);

CREATE TABLE rankings (
    id                  UUID PRIMARY KEY,
    type                VARCHAR(20) NOT NULL,
    genre               VARCHAR(100),
    platform            VARCHAR(20),
    post_id             UUID NOT NULL REFERENCES posts (id),
    rank_position       INTEGER NOT NULL,
    score               DOUBLE PRECISION NOT NULL,
    period_start        TIMESTAMPTZ,
    period_end          TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_rankings_type_genre_platform ON rankings (type, genre, platform);
CREATE INDEX idx_rankings_rank_position ON rankings (rank_position);

CREATE TABLE saved_analyses (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL REFERENCES users (id),
    post_id         UUID NOT NULL REFERENCES posts (id),
    note            VARCHAR(1000),
    created_at      TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_saved_analyses_user_id ON saved_analyses (user_id);
