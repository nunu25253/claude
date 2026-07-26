# Phase9: 競合分析（AI差分説明）

## 1. 目的

アカウント単位で投稿頻度・平均再生・平均コメント・平均いいね・投稿時間・投稿形式・投稿ジャンル・投稿長さを算出し、AIが競合との差を説明する。

## 2. セルフレビュー（既存機能との統合）

本プラットフォームには既に`CompetitorAnalysisApplicationService`（既存の基盤機能）があり、`CompetitorStats`集約として**平均いいね・平均コメント・投稿頻度・投稿時間帯分布・平均動画時間・平均文字数・伸びる投稿ランキング**を算出・永続化済みであることを確認した。

Phase9要求8項目との差分:

| 要求項目 | 既存実装 | 対応 |
|---|---|---|
| 投稿頻度 | ✅ `postingFrequencyPerWeek` | 流用 |
| 平均コメント | ✅ `averageCommentCount` | 流用 |
| 平均いいね | ✅ `averageLikeCount` | 流用 |
| 投稿時間 | ✅ `postingTimeDistribution` | 流用 |
| 投稿長さ | ✅ `averageCaptionLength`（+動画時間） | 流用 |
| **平均再生** | ❌ 未実装 | **新規追加**（`averageViewCount`。Phase1の教訓どおり、未計測(null)の投稿は平均から除外し、0と混同しない） |
| **投稿形式** | ❌ 未実装 | **新規追加**（`postFormatDistribution`。Phase2の`ContentFormat`分布） |
| **投稿ジャンル** | ❌ 未実装 | **新規追加**（`genreDistribution`。Phase5の`AnalysisResult.genre`分布。未分析投稿は集計対象外） |
| **AIによる競合との差の説明** | ❌ 未実装（既存は単一アカウントの統計のみ） | **新規追加**（`CompetitorComparisonApplicationService`） |

→ 既存`CompetitorStats`集約を拡張（新規テーブル新設はしない）し、比較・AI説明は新しいユースケースとして追加する。

## 3. 設計

- `CompetitorStats`に`averageViewCount`（Double、未計測投稿は集計から除外）、`postFormatDistribution`、`genreDistribution`（いずれも`Map<String, Double>`、比率）を追加。
- `CompetitorAnalysisApplicationService.aggregate()`が`PostNormalizer`/`PostPreprocessor`/`AnalysisResultRepository`を追加利用してこれらを算出する。
- 新規`AiCompetitorDifferencePort`（IF）/ `OpenAiCompetitorDifferenceService`（実装）: 2アカウント分の`CompetitorStats`を比較し、差分の自然言語説明をAIで生成。フォールバックは主要指標の数値差分を機械的に文章化。
- 新規`CompetitorComparisonApplicationService.compare(accountId, competitorAccountId)`: 既存の`getStats()`を再利用して両アカウントの統計を取得し、AI説明を付与して返す。

## 4. 実装結果

- `domain/competitor/CompetitorStats.java`: 3フィールド追加
- `backend/src/main/resources/db/migration/V7__competitor_stats_phase9_fields.sql`: カラム追加
- `infrastructure/persistence/converter/StringDoubleMapJsonConverter.java`（新規、ジャンル/形式分布用）
- `infrastructure/persistence/entity/CompetitorStatsEntity.java` / `CompetitorStatsMapper.java`: 対応
- `application/competitor/CompetitorAnalysisApplicationService.java`: 新規3項目の集計ロジックを追加
- `application/competitor/AiCompetitorDifferencePort.java`(IF), `infrastructure/external/openai/OpenAiCompetitorDifferenceService.java`
- `application/competitor/CompetitorComparisonApplicationService.java`, `dto/CompetitorComparisonResultDto.java`
- `presentation/controller/CompetitorController.java`: `GET /api/v1/competitors/{accountId}/compare/{competitorAccountId}` を追加
- テスト: 既存`CompetitorAnalysisApplicationService`にはテストが無かったため新規に追加。`OpenAiCompetitorDifferenceServiceTest`、`CompetitorComparisonApplicationServiceTest`も追加

## 5. レビュー

**懸念点・改善案**
1. `averageViewCount`は未計測投稿を除外して算出するため、母数が少ないアカウント（例: Xのみ運用）では信頼性が下がる可能性がある。将来的には算出に使った母数（サンプル数）もレスポンスに含めることを検討する。
2. ジャンル分布は表記揺れの影響を受ける（Phase5レビューで既出の課題）。

## 6. Phase10プレビュー

Phase10（企画生成AI）では、Phase8（共通点分析）の結果を元に20件の投稿企画を生成する。実装前のセルフレビューでは、「20件を一度のプロンプトで生成する場合のJSON構造化の信頼性」「企画の永続化要否（保存済み分析との関係）」を中心に整理する。
