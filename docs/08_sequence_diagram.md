# 08. シーケンス図

本ドキュメントは代表的な2つの主要フロー（投稿URL分析、競合分析）に加え、関連する補助フロー（AIレポート生成）をMermaidシーケンス図で示します。登場するクラス名は `07_class_diagram.md` に対応します。

## 1. 投稿URL分析フロー

「URL入力 → データ取得 → AI分析 → BuzzScore算出 → 改善案生成 → 類似投稿提案 → 保存」までの一連の流れです。

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant FE as Frontend<br/>(SCR-007/SCR-008)
    participant API as PostAnalysisController
    participant UC as AnalyzePostUrlUseCase
    participant Factory as PlatformFactory
    participant SP as SocialPlatform実装<br/>(Instagram/TikTok/X Service)
    participant Ext as 各SNS公式API
    participant PostRepo as PostRepository
    participant AiPort as AiAnalysisPort<br/>(OpenAiAnalysisAdapter)
    participant OpenAI as OpenAI API
    participant Calc as BuzzScoreCalculator
    participant ResultRepo as AnalysisResultRepository
    participant Cache as Redis Cache
    participant Pub as ApplicationEventPublisher

    User->>FE: 投稿URLを入力し「分析実行」を押下
    FE->>API: POST /api/v1/posts/analyze { postUrl }
    API->>UC: execute(AnalyzePostUrlCommand)

    UC->>Cache: 同一URLの分析結果キャッシュを確認
    alt キャッシュヒット（既存分析あり）
        Cache-->>UC: 既存 AnalysisResult
        UC-->>API: 既存分析結果を返却
        API-->>FE: 200 OK（キャッシュ済み結果）
    else キャッシュなし（新規分析）
        UC->>Factory: resolveFromUrl(postUrl)
        Factory-->>UC: 対応する SocialPlatform実装（例: InstagramService）
        UC->>SP: fetchPostByUrl(postUrl)
        SP->>Ext: 公式API呼び出し（公開投稿データ取得）
        Ext-->>SP: 投稿データ（いいね数/コメント数/再生数/キャプション等 公開情報のみ）
        SP-->>UC: PostData

        UC->>PostRepo: findByPlatformPostId(...) / save(Post)
        PostRepo-->>UC: 保存済み Post（hashtags/post_metricsも保存）

        UC->>AiPort: analyze(post, promptContext)
        AiPort->>OpenAI: プロンプト送信（AnalysisPromptBuilderで構築）
        OpenAI-->>AiPort: バズ理由/ターゲット層/フック/CTA/感情/構成/文章/投稿時間/ハッシュタグ分析
        AiPort-->>UC: AnalysisResult（改善提案含む）

        UC->>Calc: calculate(BuzzScoreContext)
        Calc->>Calc: 8つのBuzzScoreStrategyを合成
        Calc-->>UC: BuzzScore（内訳付き 0-100点）

        UC->>UC: 類似投稿検索（BuzzScore/ジャンル/投稿種別が近い過去投稿を抽出）
        UC->>ResultRepo: save(AnalysisResult), save(BuzzScore)
        ResultRepo-->>UC: 保存完了

        UC->>Pub: publish(PostAnalyzedEvent)
        Note right of Pub: Observer: レポート自動生成候補通知/<br/>ランキング再計算トリガ/キャッシュ更新は非同期リスナーが処理

        UC-->>API: AnalysisResult + BuzzScore + 改善案 + 類似投稿
        API-->>FE: 200/202 分析結果
    end

    FE-->>User: SCR-008 分析結果詳細画面を表示

    opt ユーザーが保存を選択
        User->>FE: 「保存」ボタン押下
        FE->>API: POST /api/v1/saved-analyses { analysisResultId }
        API-->>FE: 201 Created（保存済み分析へ反映）
    end
```

## 2. 競合分析フロー

「競合アカウント登録 → 投稿一括取得 → 統計集計 → ランキング表示」の流れです。

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant FE as Frontend<br/>(SCR-005/SCR-006)
    participant API as CompetitorController
    participant RegUC as RegisterSocialAccountUseCase
    participant Factory as PlatformFactory
    participant SP as SocialPlatform実装
    participant Ext as 各SNS公式API
    participant AccRepo as SocialAccountRepository
    participant PostRepo as PostRepository
    participant AnalyzeUC as AnalyzeCompetitorUseCase
    participant Aggregator as CompetitorStatsAggregator
    participant StatsRepo as CompetitorStatsRepository
    participant Cache as Redis Cache

    User->>FE: 競合アカウントのプロフィールURL/ユーザー名を入力
    FE->>API: POST /api/v1/social-accounts { platform, username }
    API->>RegUC: execute(RegisterSocialAccountCommand)
    RegUC->>Factory: resolve(platformCode)
    Factory-->>RegUC: SocialPlatform実装
    RegUC->>SP: fetchAccountProfile(username)
    SP->>Ext: 公式API呼び出し（公開プロフィール取得）
    Ext-->>SP: プロフィール情報（フォロワー数等 公開分のみ）
    SP-->>RegUC: SocialAccountData
    RegUC->>AccRepo: save(SocialAccount)
    AccRepo-->>RegUC: 保存済み SocialAccount

    RegUC->>SP: fetchAccountPosts(accountId, since)
    SP->>Ext: 公式API呼び出し（公開投稿一覧取得）
    Ext-->>SP: 投稿データ一覧（複数件）
    SP-->>RegUC: List~PostData~
    RegUC->>PostRepo: saveAll(posts)
    PostRepo-->>RegUC: 保存完了
    RegUC-->>API: SocialAccount登録完了
    API-->>FE: 201 Created

    FE-->>User: SCR-005 一覧に追加を表示

    User->>FE: 競合アカウントを選択（SCR-006へ遷移）
    FE->>API: GET /api/v1/competitors/{accountId}/analysis?period=WEEKLY
    API->>AnalyzeUC: execute(GetCompetitorStatsQuery)

    AnalyzeUC->>Cache: 集計結果キャッシュ確認
    alt キャッシュあり
        Cache-->>AnalyzeUC: CompetitorStats
    else キャッシュなし
        AnalyzeUC->>PostRepo: findBySocialAccountId(accountId, period)
        PostRepo-->>AnalyzeUC: 対象期間の投稿一覧
        AnalyzeUC->>Aggregator: aggregate(posts)
        Aggregator-->>AnalyzeUC: 平均いいね数/平均コメント数/投稿頻度/<br/>平均動画時間/平均文字数/投稿時間帯傾向/ジャンル傾向
        AnalyzeUC->>StatsRepo: save(CompetitorStats)
        AnalyzeUC->>Cache: キャッシュ更新
    end

    AnalyzeUC->>PostRepo: findTopPostsByBuzzScore(accountId, limit)
    PostRepo-->>AnalyzeUC: 伸びる投稿ランキング（アカウント内）
    AnalyzeUC-->>API: CompetitorStats + 伸びる投稿ランキング
    API-->>FE: 200 OK
    FE-->>User: SCR-006 競合分析詳細画面を表示（統計 + ランキング + ヒートマップ）
```

## 3. AIレポート生成・出力フロー（補助フロー）

投稿分析結果画面から「レポート出力」を押した際のワンクリック出力の流れです。

```mermaid
sequenceDiagram
    actor User as ユーザー
    participant FE as Frontend<br/>(SCR-008/SCR-010)
    participant API as ReportController
    participant UC as GenerateReportUseCase
    participant Builder as ReportContentBuilder
    participant ResultRepo as AnalysisResultRepository
    participant ExpFactory as ReportExporterFactory
    participant Exporter as ReportExportStrategy実装<br/>(Pdf/Markdown/Html)
    participant Storage as StoragePort<br/>(S3互換ストレージ)
    participant ReportRepo as ReportRepository

    User->>FE: 出力形式（PDF/Markdown/HTML）を選択し「レポート出力」を押下
    FE->>API: POST /api/v1/reports { analysisResultId, format }
    API->>UC: execute(GenerateReportCommand)

    UC->>ResultRepo: findById(analysisResultId)
    ResultRepo-->>UC: AnalysisResult + BuzzScore + Post

    UC->>Builder: withPostSummary(post).withBuzzScore(score)<br/>.withAiAnalysis(result).withImprovementSuggestions(list)
    Builder-->>UC: ReportContent

    UC->>ExpFactory: resolve(format)
    ExpFactory-->>UC: 対応する ReportExportStrategy
    UC->>Exporter: export(reportContent)
    Exporter-->>UC: ReportFile（バイナリ/テキスト）

    UC->>Storage: upload(reportFile)
    Storage-->>UC: fileUrl

    UC->>ReportRepo: save(Report{status=COMPLETED, fileUrl})
    ReportRepo-->>UC: 保存完了

    UC-->>API: Report（ダウンロードURL付き）
    API-->>FE: 201 Created
    FE-->>User: SCR-010 プレビュー表示 + ダウンロードボタン活性化
```
