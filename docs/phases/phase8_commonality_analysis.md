# Phase8: 共通点分析

## 1. 目的

検索結果（最大100件程度の投稿群）から、共通タイトル・共通フック・共通CTA・共通構成・共通動画時間・共通投稿時間・共通ハッシュタグ・共通ターゲットをAIで抽出する。

## 2. セルフレビュー（トークン数・コスト制約への対処）

- 8項目のうち**動画時間・投稿時間・ハッシュタグは統計的に決定的に算出可能**（Phase2の`PreprocessedPost`から平均/最頻値/頻度集計するだけで済み、AI呼び出し不要）。
- 残る**タイトル・フック・CTA・構成・ターゲットの「共通パターン」抽出は自由文の意味的な傾向把握が必要**なため、OpenAI Chat Completions APIを利用する。
- **既知の制約・代替案**: 100件全ての分析結果全文をプロンプトに含めると、トークン数・コストが過大になる（1件あたり5項目×数百文字を100件分渡すと数万トークン規模になりうる）。→ **代替案**: AIへのプロンプトには要約済みスニペット（各項目を最大80文字に切り詰め）を、かつ**サンプル上限30件**までに絞って渡す設計とする。統計項目（動画時間・投稿時間・ハッシュタグ）は入力全件を対象に決定的に集計するため、サンプリングの影響を受けない。
- AnalysisResult（Phase5）が存在しない投稿（未分析）は、AI分析パートの対象からは除外するが、統計項目の集計には含める（Post/PreprocessedPostは常に存在するため）。
- 結論: **実装可能**。上記のサンプリング方針を明記した上で進める。

## 3. 設計

- 統計集計（AI不使用、決定的）: `CommonalityStatisticsCalculator`（ドメインサービス）— ハッシュタグ頻度Top5、動画時間の中央値、投稿時間帯の最頻値、コンテンツ形式の最頻値。
- AI分析: `AiCommonalityAnalysisPort`（IF）/ `OpenAiCommonalityAnalysisService`（実装）— サンプリングされた投稿の要約スニペットから共通パターンをJSON形式で抽出。
- `CommonalityAnalysisApplicationService.analyze(List<UUID> postIds)`が両者を統合して返す（永続化はしない。都度計算するオンデマンド分析、Phase4/6と同方針）。

## 4. 実装結果

- `domain/commonality/`: `CommonalityAnalysisResult`(Builder), `CommonalityStatisticsCalculator`
- `application/commonality/AiCommonalityAnalysisPort.java`(IF), `dto/CommonalityAnalysisResultDto.java`
- `infrastructure/external/openai/OpenAiCommonalityAnalysisService.java`: サンプル30件・スニペット80文字で要約したプロンプトを構築。APIキー未設定時は統計的な頻出語ベースの簡易フォールバックを返す
- `application/commonality/CommonalityAnalysisApplicationService.java`
- `presentation/controller/CommonalityAnalysisController.java`: `POST /api/v1/commonality/analyze`（リクエストボディに投稿ID配列、最大100件）
- テスト: `CommonalityStatisticsCalculatorTest`、`OpenAiCommonalityAnalysisServiceTest`（フォールバック検証）、`CommonalityAnalysisApplicationServiceTest`（Mockito）

## 5. レビュー

**懸念点・改善案**
1. サンプル30件の選び方は「入力リストの先頭30件」という単純な方針。将来的には一致率・ランキングスコア上位から均等にサンプリングする等、代表性を高める余地がある。
2. AIのJSON応答が期待するキーを含まない場合のフォールバック処理は既存の`OpenAiAnalysisService`と同様のパターンを踏襲しているが、共通点分析特有の「該当データなし」ケースのハンドリングは今回新規に実装した。

## 6. Phase9プレビュー

Phase9（競合分析）ではアカウント単位で投稿頻度・平均再生・平均コメント・平均いいね・投稿時間・投稿形式・投稿ジャンル・投稿長さを算出し、AIが競合との差を説明する。既存の`CompetitorAnalysisApplicationService`（本プラットフォーム基盤に既存）との統合方針をセルフレビューで整理する。
