-- Phase5: 投稿分析AI拡張。既存の analysis_results テーブル/AnalysisResult集約に
-- ジャンル・サブジャンル・投稿目的・投稿構成分析・強み・弱みを追加する
-- (新しいテーブルを新設せず既存集約を拡張する方針。docs/phases/phase5_post_analysis.md 参照)。
ALTER TABLE analysis_results
    ADD COLUMN genre                    VARCHAR(100),
    ADD COLUMN sub_genre                VARCHAR(100),
    ADD COLUMN post_purpose             VARCHAR(100),
    ADD COLUMN post_structure_analysis  TEXT,
    ADD COLUMN strengths                TEXT,
    ADD COLUMN weaknesses               TEXT;

-- V2で投入済みのサンプル分析結果に、Phase5で追加した項目を補完する（デモ・動作確認用）。
UPDATE analysis_results SET
    genre = 'グルメ', sub_genre = 'スイーツ', post_purpose = '認知獲得',
    post_structure_analysis = 'フック(ビジュアル)→商品紹介→CTA(来店誘導)の順に構成されている。',
    strengths = '季節限定訴求とビジュアルの美しさ。', weaknesses = 'CTAが弱く来店特典等の訴求余地がある。'
WHERE id = 'd0000000-0000-0000-0000-000000000001';

UPDATE analysis_results SET
    genre = 'フィットネス', sub_genre = '宅トレ', post_purpose = 'エンゲージメント獲得',
    post_structure_analysis = '時短訴求のフック→実演→継続喚起のCTAで構成されている。',
    strengths = '「5分でできる」という具体的な時短訴求。', weaknesses = 'シリーズ化やQ&A企画で継続エンゲージメントを狙う余地がある。'
WHERE id = 'd0000000-0000-0000-0000-000000000002';

UPDATE analysis_results SET
    genre = 'エンタメ', sub_genre = 'ダンスチャレンジ', post_purpose = 'フォロワー獲得',
    post_structure_analysis = '冒頭0.5秒でフック→振り付け実演→参加型CTAの構成。',
    strengths = 'トレンド音源と参加型フォーマット。', weaknesses = 'デュエット機能の活用等、さらなる参加導線の余地がある。'
WHERE id = 'd0000000-0000-0000-0000-000000000003';

UPDATE analysis_results SET
    genre = 'グルメ', sub_genre = '時短レシピ', post_purpose = '商品訴求',
    post_structure_analysis = '時短訴求フック→調理工程→完成品提示の3段構成。',
    strengths = '具体的な時短訴求と分かりやすい調理工程。', weaknesses = '材料リストの画面テキスト補足に余地がある。'
WHERE id = 'd0000000-0000-0000-0000-000000000004';

UPDATE analysis_results SET
    genre = 'テクノロジー', sub_genre = 'ビジネスニュース', post_purpose = '認知獲得',
    post_structure_analysis = '速報性フック→スレッド形式での詳細展開。',
    strengths = '速報性と専門性の高さ。', weaknesses = '続報投稿によるエンゲージメント維持の余地がある。'
WHERE id = 'd0000000-0000-0000-0000-000000000005';
