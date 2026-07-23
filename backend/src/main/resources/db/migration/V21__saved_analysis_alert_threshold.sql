-- 保存済み分析ごとにBuzzScoreのしきい値アラートを設定できるようにする。
-- alert_threshold が設定され、現在のBuzzScoreがしきい値以上、かつ未通知(alert_triggered_at IS NULL)の
-- ものだけがバッチ通知の対象になる。しきい値を再設定するとalert_triggered_atはクリアされ再度アーム状態になる。
ALTER TABLE saved_analyses ADD COLUMN alert_threshold DOUBLE PRECISION;
ALTER TABLE saved_analyses ADD COLUMN alert_triggered_at TIMESTAMPTZ;

CREATE INDEX idx_saved_analyses_pending_alert
    ON saved_analyses (alert_threshold)
    WHERE alert_threshold IS NOT NULL AND alert_triggered_at IS NULL;
