-- マルチアカウント/チーム機能の土台。個人アカウント前提でBtoB販売が困難な問題への対応。
CREATE TABLE organizations (
    id          UUID PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL
);

CREATE TABLE organization_memberships (
    id               UUID PRIMARY KEY,
    organization_id  UUID NOT NULL REFERENCES organizations (id),
    user_id          UUID NOT NULL REFERENCES users (id),
    role             VARCHAR(20) NOT NULL,
    joined_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_organization_memberships_org_user UNIQUE (organization_id, user_id)
);

CREATE INDEX idx_organization_memberships_user_id ON organization_memberships (user_id);
CREATE INDEX idx_organization_memberships_organization_id ON organization_memberships (organization_id);
