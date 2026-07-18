# Phase17: ダッシュボード（AI企画・投稿評価画面の追加）

## 1. 目的

要求仕様の9画面（ホーム/ランキング/AI分析/トレンド/競合分析/投稿評価/AI企画/レポート/設定）のうち、Stage A（初期構築）で未実装だった「AI企画」「投稿評価」の2画面をNext.jsフロントエンドに追加する。

## 2. セルフレビュー（既存フロントエンドとの統合）

### 2.1 Phase1-16で実装した多数のAPIをどの画面にどうマッピングするか

既存フロントエンド（`frontend/app/(dashboard)/`）は、ホーム・ランキング・投稿分析（AI分析）・トレンド・競合分析・レポート・設定・保存済み分析の7画面が既に実装済みであることを確認した（`components/layout/nav-items.ts`参照）。要求仕様9画面のうち残る2画面は「投稿評価」「AI企画」。

- **AI企画画面**（新規、`/proposals`）: Phase10（企画生成AI）を中心に据え、投稿ID群から企画を生成・一覧表示する。各企画カードから、Phase11（台本生成）・Phase12（カルーセル生成）をその場で呼び出して結果を展開表示できるようにする（Phase10-12を1画面に集約）。
- **投稿評価画面**（新規、`/evaluations`）: Phase14（投稿評価AI）のフォーム入力→評価結果表示。

Phase13（画像生成プロンプト）・Phase15（トレンド分析の新API）・Phase16（RAG）は、既存の「トレンド」画面や新規画面に無理に詰め込まず、**本フェーズのスコープ外**とする（要求仕様の9画面に直接対応する項目ではないため。台本/カルーセル生成の中で画像生成プロンプトの取得ボタンを追加する程度の拡張は可能だが、UIの複雑化を避けるため見送る）。

### 2.2 既存のNext.jsダッシュボード基盤とどう統合するか

既存の規約（`lib/api/*.ts` + `lib/hooks/use-*.ts` + `lib/types/*.ts` + `app/(dashboard)/*/page.tsx`、TanStack Query、react-hook-form + zod、`components/ui/*`の共通コンポーネント）にそのまま従う。新規追加するAPIクライアントは、既存の`apiClient`（`lib/api-client.ts`）をそのまま使う。

**既存の乖離との関係**: 既存フロントエンドの一部画面（トレンド等）は、初期設計時点の理想化されたAPI（`GET /trends`等）を前提に実装されている（バックエンド実装は`/trends/analyze`等に乖離している、`docs/openapi.yaml`に既述の通り）。今回新規追加する「AI企画」「投稿評価」画面は、Phase10/11/12/14で実装済みの**実際のバックエンドAPI**（`/proposals/*`, `/scripts/*`, `/carousels/*`, `/evaluations/*`）にそのまま対応させる（新規画面のため乖離が生じない）。

## 3. 実装結果

- `lib/types/proposal.ts`: `ContentProposal`, `VideoScript`, `Carousel`, `ScriptCut`, `CarouselPage`, 各種リクエスト型
- `lib/types/evaluation.ts`: `ContentEvaluation`, `PostEvaluationRequest`
- `lib/api/proposals.ts`: `POST /proposals/generate`, `GET /proposals/{generationId}`, `POST /scripts/generate`, `POST /carousels/generate`
- `lib/api/evaluations.ts`: `POST /evaluations`
- `lib/hooks/use-proposals.ts`, `lib/hooks/use-evaluations.ts`: TanStack Query（`useMutation`）ラッパー
- `components/dashboard/proposals/*`: 企画生成フォーム・企画カード（台本/カルーセル生成ボタン付き）
- `components/dashboard/evaluations/*`: 評価フォーム・評価結果表示
- `app/(dashboard)/proposals/page.tsx`, `app/(dashboard)/evaluations/page.tsx`
- `components/layout/nav-items.ts`: 「AI企画」「投稿評価」を追加（要求仕様の9画面が揃う）

## 4. レビュー

### 4.1 懸念点

- 実際のバックエンド起動・ブラウザでの動作確認は、このサンドボックス環境ではフロントエンド開発サーバー・バックエンドAPI・PostgreSQLをフル起動しての検証が難しいため、型チェック（`tsc`）とビルド（`next build`）による静的検証に留める。

### 4.2 改善案

- Phase13（画像生成プロンプト）・Phase16（RAG）のUI統合、および既存画面（トレンド等）の理想化APIから実装APIへの追従は、別タスクとして扱う。

## 5. 最終成果物への反映

Phase1-17完了後の最終成果物として、`docs/`配下の要件定義・システム設計・API設計を更新し、GitHub Actions（CI）・README・デプロイ手順を整備する（本ドキュメントの後続コミットで対応）。
