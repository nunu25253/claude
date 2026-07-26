# Phase7: ランキングAI（総合ランキングスコア）

## 1. 目的

一致率・エンゲージメント率・コメント率・いいね率・投稿鮮度・AI分析・投稿構成・ハッシュタグ・動画時間を総合したランキングスコアで投稿を順位付けする。重み付けはStrategyパターンで実装する。

## 2. セルフレビュー（既存機能との重複整理）

Phase7要求の9項目を既存実装と照合したところ、**5項目（エンゲージメント率・コメント率・AI分析・投稿構成・ハッシュタグ）は既に`BuzzScoreCalculator`（既存の投稿URL分析フロー）でカバー済み**であることが判明した。

- 二重計上を避けるため、Phase7では**既存の`BuzzScore`（保存済みの値をそのまま再利用）を1つの統合Strategyとして扱う**。
- 真に新規なのは: **一致率**(Phase6の成果)、**いいね率**（`BuzzScore`の「エンゲージメント率」はいいね+コメント合算のため、いいね単体の比率は未実装）、**投稿鮮度**、**動画時間**（`BuzzScore`の「投稿時間」は時刻帯の分析であり、動画の「長さ」は別概念）の4項目。
- ランキング対象は「Phase6のユーザー条件分析結果」を再ランキングする用途とし、既存の「急上昇/週間/月間」グローバルランキング（`PeriodicSyncApplicationService`が算出、プラットフォーム横断の話題性ランキング）とは別の新規ユースケースとする（既存の安定動作しているバッチ処理を変更しない）。
- 結論: **実装可能**。

## 3. 設計

`RankingScoreStrategy`（Strategyパターン）を5種実装し、`RankingScoreCalculator`が重み付き平均で0〜100のランキングスコアを算出する。

| Strategy | 重み | 内容 |
|---|---|---|
| MatchRateRankingStrategy | 0.30 | Phase6で算出した一致率をそのまま利用 |
| BuzzScoreRankingStrategy | 0.30 | 既存`BuzzScore`（エンゲージメント率/コメント率/AI分析/投稿構成/ハッシュタグを内包）を再利用 |
| LikeRateRankingStrategy | 0.15 | いいね数/再生数比率（新規） |
| FreshnessRankingStrategy | 0.15 | 投稿からの経過時間による指数減衰スコア（新規） |
| VideoDurationRankingStrategy | 0.10 | 動画時間が最適レンジ(15〜60秒)に近いか（新規、一般的なショート動画のベストプラクティスに基づくヒューリスティック） |

`ContentRankingApplicationService`は、Phase6の`UserConditionMatchApplicationService.evaluate()`で候補と一致率を取得し、各候補についてBuzzScore・AI分析結果・前処理結果を取得して`RankingScoreCalculator`でスコアリング、降順にソートして返す。

## 4. 実装結果

- `domain/rankingscore/`: `RankingScoreInput`, `strategy/RankingScoreStrategy`(IF)と5実装, `RankingScoreCalculator`
- `infrastructure/config/RankingScoreConfig.java`: Strategy群のBean配線
- `application/rankingscore/ContentRankingApplicationService.java`, `dto/RankingScoreResultDto.java`
- `presentation/controller/ContentRankingController.java`: `POST /api/v1/ranking-score/rank`
- テスト: 各Strategy・Calculator・ApplicationServiceの単体テスト

## 5. レビュー

**懸念点・改善案**
1. `VideoDurationRankingStrategy`の最適レンジ(15〜60秒)は一般論に基づく固定値であり、ジャンルやプラットフォームごとの最適値は異なりうる。将来的にはPhase9（競合分析）の統計値から動的に算出することが望ましい。
2. `FreshnessRankingStrategy`の減衰定数は仮値。実データでの効果検証（新しい投稿ばかりが上位に来すぎないか等）が今後必要。

## 6. Phase8プレビュー

Phase8（共通点分析）では、検索結果100件から共通タイトル・共通フック・共通CTA・共通構成・共通動画時間・共通投稿時間・共通ハッシュタグ・共通ターゲットをAIで抽出する。実装前のセルフレビューでは、「AIに100件分の分析結果をまとめて渡すプロンプト設計」「トークン数の制約への対処（要約してから渡す等）」を中心に整理する。
