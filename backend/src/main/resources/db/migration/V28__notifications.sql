-- アプリ内通知センター(しきい値アラート・週次ダイジェストのメール通知に加え、
-- アプリ内でも同時に通知を届けるための土台)。
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    link VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    read_at TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user_id_created_at ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_user_id_unread ON notifications (user_id) WHERE read_at IS NULL;
