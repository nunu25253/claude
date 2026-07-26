-- サンプルデータ（開発・デモ環境用）。本番環境では投入しないこと。

-- ============ users ============
-- password_hash はいずれも平文 "Password123!" をBCryptでハッシュ化した値
INSERT INTO users (id, email, password_hash, display_name, role, created_at, updated_at) VALUES
('a0000000-0000-0000-0000-000000000001', 'admin@buzzanalysis.example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa1S3s3s3s3s3s3s3s3s3s3s3s3s3s3u', '管理者', 'ADMIN', now() - interval '90 days', now() - interval '90 days'),
('a0000000-0000-0000-0000-000000000002', 'marketer@buzzanalysis.example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa1S3s3s3s3s3s3s3s3s3s3s3s3s3s3u', 'マーケター太郎', 'USER', now() - interval '60 days', now() - interval '60 days'),
('a0000000-0000-0000-0000-000000000003', 'creator@buzzanalysis.example.com', '$2a$10$7EqJtq98hPqEX7fNZaFWoOa1S3s3s3s3s3s3s3s3s3s3s3s3s3s3u', 'クリエイター花子', 'USER', now() - interval '30 days', now() - interval '30 days');

-- ============ social_accounts ============
INSERT INTO social_accounts (id, platform, external_account_id, username, display_name, profile_url, follower_count, post_count, created_at, updated_at) VALUES
('b0000000-0000-0000-0000-000000000001', 'INSTAGRAM', 'ig_ext_001', 'trendy_cafe_jp', 'Trendy Cafe Japan', 'https://www.instagram.com/trendy_cafe_jp/', 452000, 812, now() - interval '90 days', now() - interval '1 days'),
('b0000000-0000-0000-0000-000000000002', 'INSTAGRAM', 'ig_ext_002', 'fitness_daily_jp', 'Fitness Daily', 'https://www.instagram.com/fitness_daily_jp/', 189000, 1420, now() - interval '80 days', now() - interval '2 days'),
('b0000000-0000-0000-0000-000000000003', 'TIKTOK', 'tt_ext_001', 'dance_kingdom', 'Dance Kingdom', 'https://www.tiktok.com/@dance_kingdom', 980000, 305, now() - interval '70 days', now() - interval '1 days'),
('b0000000-0000-0000-0000-000000000004', 'TIKTOK', 'tt_ext_002', 'cooking_hacks_jp', 'Cooking Hacks JP', 'https://www.tiktok.com/@cooking_hacks_jp', 320000, 540, now() - interval '65 days', now() - interval '3 days'),
('b0000000-0000-0000-0000-000000000005', 'X', 'x_ext_001', 'tech_news_jp', 'Tech News JP', 'https://x.com/tech_news_jp', 275000, 15300, now() - interval '60 days', now() - interval '1 days');

-- ============ posts ============
INSERT INTO posts (id, social_account_id, platform, external_id, url, published_at, author_name, caption, like_count, comment_count, view_count, share_count, video_duration_seconds, image_count, post_type, created_at, updated_at) VALUES
('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 'INSTAGRAM', 'ig_post_001', 'https://www.instagram.com/reel/ig_post_001/', now() - interval '10 days', 'trendy_cafe_jp', '新作の抹茶フラペチーノが可愛すぎる😍\n\n夏限定なのでお早めに！\n\n#カフェ巡り #新作スイーツ #東京カフェ', 48500, 1230, 890000, 3400, 28, NULL, 'REEL', now() - interval '10 days', now() - interval '1 days'),
('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 'INSTAGRAM', 'ig_post_002', 'https://www.instagram.com/p/ig_post_002/', now() - interval '25 days', 'trendy_cafe_jp', '店内の新しいインテリアをご紹介します。\n\n#カフェインテリア #おしゃれカフェ', 12300, 210, NULL, NULL, NULL, 5, 'CAROUSEL', now() - interval '25 days', now() - interval '5 days'),
('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002', 'INSTAGRAM', 'ig_post_003', 'https://www.instagram.com/reel/ig_post_003/', now() - interval '5 days', 'fitness_daily_jp', '5分でできる自宅トレーニング！\n\n毎日続けて理想の体へ💪\n\n#宅トレ #フィットネス #筋トレ女子', 76200, 2450, 1500000, 8900, 45, NULL, 'REEL', now() - interval '5 days', now() - interval '1 days'),
('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000003', 'TIKTOK', 'tt_post_001', 'https://www.tiktok.com/@dance_kingdom/video/tt_post_001', now() - interval '3 days', 'dance_kingdom', '新しいダンスチャレンジやってみた！みんなも挑戦してね #fyp #おすすめ #ダンスチャレンジ', 342000, 15200, 4800000, 62000, 22, NULL, 'VIDEO', now() - interval '3 days', now() - interval '1 days'),
('c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000003', 'TIKTOK', 'tt_post_002', 'https://www.tiktok.com/@dance_kingdom/video/tt_post_002', now() - interval '20 days', 'dance_kingdom', '練習の裏側を少しだけ公開 #dance #practice', 45000, 980, 620000, 3100, 35, NULL, 'VIDEO', now() - interval '20 days', now() - interval '4 days'),
('c0000000-0000-0000-0000-000000000006', 'b0000000-0000-0000-0000-000000000004', 'TIKTOK', 'tt_post_003', 'https://www.tiktok.com/@cooking_hacks_jp/video/tt_post_003', now() - interval '7 days', 'cooking_hacks_jp', '3分で作れる時短レシピ！忙しい日の味方です #時短レシピ #料理 #Life Hack', 128000, 4300, 2100000, 19500, 58, NULL, 'VIDEO', now() - interval '7 days', now() - interval '1 days'),
('c0000000-0000-0000-0000-000000000007', 'b0000000-0000-0000-0000-000000000005', 'X', 'x_post_001', 'https://x.com/tech_news_jp/status/x_post_001', now() - interval '2 days', 'tech_news_jp', '速報: 新しいAIモデルが発表されました。従来比2倍の性能とのこと。 #AI #テクノロジー #速報', 18900, 1200, 3200000, 8700, NULL, NULL, 'TEXT', now() - interval '2 days', now() - interval '1 days'),
('c0000000-0000-0000-0000-000000000008', 'b0000000-0000-0000-0000-000000000005', 'X', 'x_post_002', 'https://x.com/tech_news_jp/status/x_post_002', now() - interval '15 days', 'tech_news_jp', '今週のテック業界まとめ。詳細はスレッドで。 #週間まとめ', 5400, 320, 450000, 890, NULL, NULL, 'TEXT', now() - interval '15 days', now() - interval '6 days');

-- ============ post_hashtags ============
INSERT INTO post_hashtags (post_id, hashtag) VALUES
('c0000000-0000-0000-0000-000000000001', 'カフェ巡り'),
('c0000000-0000-0000-0000-000000000001', '新作スイーツ'),
('c0000000-0000-0000-0000-000000000001', '東京カフェ'),
('c0000000-0000-0000-0000-000000000002', 'カフェインテリア'),
('c0000000-0000-0000-0000-000000000002', 'おしゃれカフェ'),
('c0000000-0000-0000-0000-000000000003', '宅トレ'),
('c0000000-0000-0000-0000-000000000003', 'フィットネス'),
('c0000000-0000-0000-0000-000000000003', '筋トレ女子'),
('c0000000-0000-0000-0000-000000000004', 'fyp'),
('c0000000-0000-0000-0000-000000000004', 'おすすめ'),
('c0000000-0000-0000-0000-000000000004', 'ダンスチャレンジ'),
('c0000000-0000-0000-0000-000000000005', 'dance'),
('c0000000-0000-0000-0000-000000000005', 'practice'),
('c0000000-0000-0000-0000-000000000006', '時短レシピ'),
('c0000000-0000-0000-0000-000000000006', '料理'),
('c0000000-0000-0000-0000-000000000007', 'AI'),
('c0000000-0000-0000-0000-000000000007', 'テクノロジー'),
('c0000000-0000-0000-0000-000000000007', '速報'),
('c0000000-0000-0000-0000-000000000008', '週間まとめ');

-- ============ analysis_results ============
INSERT INTO analysis_results (id, post_id, why_it_went_viral, target_audience, hook, call_to_action, sentiment_analysis, video_structure_analysis, carousel_structure_analysis, title_analysis, text_analysis, posting_time_analysis, hashtag_analysis, improvement_suggestions, created_at) VALUES
('d0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', '季節限定という希少性と、ビジュアルの美しさが拡散を後押ししました。', '20代女性、スイーツ好き、カフェ巡りが趣味の層', '冒頭の商品アップショットが視覚的インパクトを与えています。', 'プロフィールリンクから店舗情報へ誘導しています。', '全体的にポジティブな反応が多く見られます。', '商品ショット→内観→提供シーンの3部構成でテンポが良い。', '該当なし（リール投稿）', '「可愛すぎる」という感嘆表現が興味を引いています。', '絵文字と改行を効果的に使い読みやすい構成です。', '夕方18時台の投稿でアクティブユーザーが多い時間帯です。', '3個のハッシュタグが的確にジャンルを示しています。', 'CTAをより明確にし、来店特典等を訴求すると更なる効果が期待できます。', now() - interval '9 days'),
('d0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000003', 'すぐ真似できる手軽さと、継続を促すメッセージが共感を呼びました。', '在宅時間が多い20〜30代、運動初心者層', '「5分でできる」という時短訴求が強力なフックです。', '「毎日続けて」という継続喚起がCTAとして機能しています。', 'モチベーションを高めるポジティブなトーンです。', '準備→エクササイズ実演→まとめの構成で分かりやすい。', '該当なし（リール投稿）', '具体的な時間数を示したタイトルが行動を促します。', '簡潔で行動喚起力のある文章です。', '朝7時台の投稿で通勤前のユーザーにリーチしています。', '3個のハッシュタグでフィットネス層に的確にリーチしています。', '継続シリーズ化し、コメントでの質問に答える企画を追加すると良いでしょう。', now() - interval '4 days'),
('d0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000004', 'トレンドの音源と参加型フォーマットが拡散を加速させました。', 'トレンドに敏感な10代〜20代前半層', '冒頭0.5秒でダンスの決めポーズを見せるフックです。', '「みんなも挑戦してね」という参加型CTAが効果的です。', '楽しさが伝わるポジティブな反応が大多数です。', '振り付けが3段階で構成されテンポよく展開しています。', '該当なし（動画投稿）', 'チャレンジ形式のタイトルが参加意欲を刺激しています。', '短く簡潔なキャプションでテンポを損なっていません。', '夜21時台の投稿でTikTokアクティブ率が高い時間帯です。', '3個のハッシュタグでfypへの露出を狙っています。', 'デュエット機能を活用したユーザー参加型企画に発展させると良いでしょう。', now() - interval '2 days'),
('d0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000006', '時短ニーズと具体的な調理工程の分かりやすさが評価されました。', '忙しい社会人・子育て世帯', '「3分で作れる」という具体的な時短訴求がフックです。', 'コメント欄でレシピ詳細を案内し再訪問を促しています。', '実用性への感謝コメントが多くポジティブです。', '材料紹介→調理工程→完成品の3段構成で分かりやすい。', '該当なし（動画投稿）', '具体的な時間を示したタイトルが説得力を持たせています。', 'テンポよく簡潔な説明文です。', '昼12時台の投稿でランチ検討層にリーチしています。', '2個のハッシュタグに絞り込み検索性を高めています。', '材料リストを画面テキストで補足するとさらに分かりやすくなります。', now() - interval '6 days'),
('d0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000007', '速報性と専門性の高さが情報拡散を後押ししました。', 'テクノロジー・ビジネス関心層', '「速報」というワードが即座に注目を集めています。', 'スレッド形式で詳細情報への導線を作っています。', '驚きと期待が入り混じったポジティブな反応です。', '該当なし（テキスト投稿）', '該当なし（テキスト投稿）', '数値を含む具体的なタイトルが説得力を持たせています。', '簡潔で情報量の多い文章構成です。', '朝9時台の投稿で始業前の情報収集層にリーチしています。', '3個のハッシュタグで関連トピックへの露出を高めています。', '続報のフォローアップ投稿でエンゲージメントを維持すると良いでしょう。', now() - interval '1 days');

-- ============ buzz_scores ============
INSERT INTO buzz_scores (id, post_id, total_score, breakdown, calculated_at) VALUES
('e0000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 78.40, '{"engagementRate":72.5,"viewCount":68.2,"commentRate":45.1,"postFormat":100.0,"hashtag":100.0,"postTiming":100.0,"contentStructure":85.0,"aiAnalysis":75.0}', now() - interval '9 days'),
('e0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000003', 88.10, '{"engagementRate":80.1,"viewCount":75.4,"commentRate":56.3,"postFormat":100.0,"hashtag":100.0,"postTiming":70.0,"contentStructure":95.0,"aiAnalysis":82.0}', now() - interval '4 days'),
('e0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000004', 94.60, '{"engagementRate":91.2,"viewCount":88.7,"commentRate":78.9,"postFormat":100.0,"hashtag":100.0,"postTiming":100.0,"contentStructure":100.0,"aiAnalysis":90.0}', now() - interval '2 days'),
('e0000000-0000-0000-0000-000000000004', 'c0000000-0000-0000-0000-000000000006', 85.30, '{"engagementRate":76.8,"viewCount":70.5,"commentRate":60.2,"postFormat":90.0,"hashtag":100.0,"postTiming":60.0,"contentStructure":90.0,"aiAnalysis":80.0}', now() - interval '6 days'),
('e0000000-0000-0000-0000-000000000005', 'c0000000-0000-0000-0000-000000000007', 71.90, '{"engagementRate":55.4,"viewCount":82.1,"commentRate":40.0,"postFormat":35.0,"hashtag":100.0,"postTiming":70.0,"contentStructure":70.0,"aiAnalysis":72.0}', now() - interval '1 days');

-- ============ competitor_stats ============
INSERT INTO competitor_stats (id, social_account_id, average_like_count, average_comment_count, posting_frequency_per_week, posting_time_distribution, average_video_duration_seconds, average_caption_length, top_performing_post_ids, calculated_at) VALUES
('f0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001', 30400.0, 720.0, 3.5, '{"18":60.0,"12":40.0}', 28.0, 45.0, '["c0000000-0000-0000-0000-000000000001","c0000000-0000-0000-0000-000000000002"]', now() - interval '1 days'),
('f0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002', 76200.0, 2450.0, 4.0, '{"7":100.0}', 45.0, 30.0, '["c0000000-0000-0000-0000-000000000003"]', now() - interval '1 days'),
('f0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000003', 193500.0, 8090.0, 2.5, '{"21":50.0,"20":50.0}', 28.5, 25.0, '["c0000000-0000-0000-0000-000000000004","c0000000-0000-0000-0000-000000000005"]', now() - interval '1 days'),
('f0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000004', 128000.0, 4300.0, 3.0, '{"12":100.0}', 58.0, 22.0, '["c0000000-0000-0000-0000-000000000006"]', now() - interval '1 days'),
('f0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000005', 12150.0, 760.0, 5.0, '{"9":50.0,"15":50.0}', NULL, 40.0, '["c0000000-0000-0000-0000-000000000007","c0000000-0000-0000-0000-000000000008"]', now() - interval '1 days');

-- ============ reports ============
INSERT INTO reports (id, post_id, format, title, storage_key, content_size_bytes, generated_at) VALUES
('10000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000001', 'MARKDOWN', 'Buzz Analysis Report - trendy_cafe_jp', 'reports/c0000000-0000-0000-0000-000000000001/sample-report.md', 2048, now() - interval '9 days'),
('10000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000004', 'PDF', 'Buzz Analysis Report - dance_kingdom', 'reports/c0000000-0000-0000-0000-000000000004/sample-report.pdf', 51200, now() - interval '2 days'),
('10000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000007', 'HTML', 'Buzz Analysis Report - tech_news_jp', 'reports/c0000000-0000-0000-0000-000000000007/sample-report.html', 3072, now() - interval '1 days');

-- ============ rankings ============
INSERT INTO rankings (id, type, genre, platform, post_id, rank_position, score, period_start, period_end, created_at) VALUES
('20000000-0000-0000-0000-000000000001', 'TRENDING', 'entertainment', 'TIKTOK', 'c0000000-0000-0000-0000-000000000004', 1, 94.60, now() - interval '3 days', now(), now()),
('20000000-0000-0000-0000-000000000002', 'TRENDING', 'lifestyle', 'INSTAGRAM', 'c0000000-0000-0000-0000-000000000003', 2, 88.10, now() - interval '3 days', now(), now()),
('20000000-0000-0000-0000-000000000003', 'TRENDING', 'food', 'TIKTOK', 'c0000000-0000-0000-0000-000000000006', 3, 85.30, now() - interval '3 days', now(), now()),
('20000000-0000-0000-0000-000000000004', 'WEEKLY', 'entertainment', 'TIKTOK', 'c0000000-0000-0000-0000-000000000004', 1, 94.60, now() - interval '7 days', now(), now()),
('20000000-0000-0000-0000-000000000005', 'WEEKLY', 'food', 'INSTAGRAM', 'c0000000-0000-0000-0000-000000000001', 2, 78.40, now() - interval '7 days', now(), now()),
('20000000-0000-0000-0000-000000000006', 'WEEKLY', 'technology', 'X', 'c0000000-0000-0000-0000-000000000007', 3, 71.90, now() - interval '7 days', now(), now()),
('20000000-0000-0000-0000-000000000007', 'MONTHLY', 'entertainment', 'TIKTOK', 'c0000000-0000-0000-0000-000000000004', 1, 94.60, now() - interval '30 days', now(), now()),
('20000000-0000-0000-0000-000000000008', 'MONTHLY', 'lifestyle', 'INSTAGRAM', 'c0000000-0000-0000-0000-000000000003', 2, 88.10, now() - interval '30 days', now(), now());

-- ============ saved_analyses ============
INSERT INTO saved_analyses (id, user_id, post_id, note, created_at) VALUES
('30000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000001', '来月のキャンペーン企画の参考に', now() - interval '8 days'),
('30000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000004', 'ダンスチャレンジ企画のベンチマーク', now() - interval '2 days'),
('30000000-0000-0000-0000-000000000003', 'a0000000-0000-0000-0000-000000000003', 'c0000000-0000-0000-0000-000000000006', '時短レシピ動画の構成参考', now() - interval '5 days');
