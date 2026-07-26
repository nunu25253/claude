-- 課金プラン(無料枠/有料枠)の実装。ユーザー単位で1件のみ持つ(未アップグレード時はレコード自体が存在しない)。
CREATE TABLE subscriptions (
    id                          UUID PRIMARY KEY,
    user_id                     UUID NOT NULL UNIQUE REFERENCES users (id),
    plan                        VARCHAR(20) NOT NULL,
    status                      VARCHAR(20) NOT NULL,
    payment_provider_member_id  VARCHAR(100),
    current_period_end          TIMESTAMPTZ,
    created_at                  TIMESTAMPTZ NOT NULL,
    updated_at                  TIMESTAMPTZ NOT NULL
);
