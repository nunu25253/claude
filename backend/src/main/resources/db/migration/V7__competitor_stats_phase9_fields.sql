-- Phase9: 競合分析拡張。既存のcompetitor_statsテーブルに平均再生数・投稿形式分布・ジャンル分布を追加する
-- (新しいテーブルを新設せず既存集約を拡張する方針。docs/phases/phase9_competitor_analysis.md 参照)。
ALTER TABLE competitor_stats
    ADD COLUMN average_view_count        DOUBLE PRECISION,
    ADD COLUMN post_format_distribution  TEXT,
    ADD COLUMN genre_distribution        TEXT;
