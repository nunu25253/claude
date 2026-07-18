# 06. ディレクトリ構成

本ドキュメントは、クリーンアーキテクチャ + DDD に基づく `backend/`（Java 21 / Spring Boot / Gradle）と、Next.js App Router に基づく `frontend/`（React / TypeScript）のディレクトリ構成を示します。

## 1. リポジトリ全体構成

```text
.
├── backend/                     # Spring Boot バックエンド（Java 21, Gradle）
├── frontend/                    # Next.js フロントエンド（React, TypeScript）
├── docs/                        # 設計ドキュメント（本ディレクトリ）
├── infra/                       # インフラ関連（Docker Compose, 将来のIaC）
│   ├── docker/
│   │   ├── backend.Dockerfile
│   │   ├── frontend.Dockerfile
│   │   └── nginx/
│   └── env/
│       ├── .env.example
│       └── docker-compose.yml
├── docker-compose.yml           # ローカル/初期運用向け Compose 定義
├── .github/
│   └── workflows/               # CI（lint/test/build）
└── README.md
```

## 2. backend/ ディレクトリ構成（クリーンアーキテクチャ）

パッケージルートは `com.snsbuzz.platform` とし、`domain` → `application` → `infrastructure` / `presentation` の依存方向を厳守します（依存性逆転の原則：`domain`/`application` は外部フレームワークに依存しない）。

```text
backend/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── wrapper/
├── src/
│   ├── main/
│   │   ├── java/com/snsbuzz/platform/
│   │   │   ├── SnsBuzzPlatformApplication.java
│   │   │   │
│   │   │   ├── domain/                          # ドメイン層（フレームワーク非依存）
│   │   │   │   ├── model/
│   │   │   │   │   ├── user/
│   │   │   │   │   │   ├── User.java
│   │   │   │   │   │   ├── UserRole.java
│   │   │   │   │   │   └── UserPlan.java
│   │   │   │   │   ├── platform/
│   │   │   │   │   │   ├── Platform.java
│   │   │   │   │   │   └── PlatformCode.java
│   │   │   │   │   ├── socialaccount/
│   │   │   │   │   │   └── SocialAccount.java
│   │   │   │   │   ├── post/
│   │   │   │   │   │   ├── Post.java
│   │   │   │   │   │   ├── PostType.java
│   │   │   │   │   │   ├── PostMetrics.java
│   │   │   │   │   │   └── Hashtag.java
│   │   │   │   │   ├── analysis/
│   │   │   │   │   │   ├── AnalysisResult.java
│   │   │   │   │   │   ├── AnalysisStatus.java
│   │   │   │   │   │   ├── SentimentAnalysis.java
│   │   │   │   │   │   ├── VideoStructureAnalysis.java
│   │   │   │   │   │   ├── CarouselStructureAnalysis.java
│   │   │   │   │   │   └── ImprovementSuggestion.java
│   │   │   │   │   ├── buzzscore/
│   │   │   │   │   │   ├── BuzzScore.java
│   │   │   │   │   │   └── BuzzScoreBreakdown.java
│   │   │   │   │   ├── competitor/
│   │   │   │   │   │   └── CompetitorStats.java
│   │   │   │   │   ├── ranking/
│   │   │   │   │   │   ├── Ranking.java
│   │   │   │   │   │   ├── RankingType.java
│   │   │   │   │   │   └── RankingEntry.java
│   │   │   │   │   ├── report/
│   │   │   │   │   │   ├── Report.java
│   │   │   │   │   │   └── ReportFormat.java
│   │   │   │   │   └── saved/
│   │   │   │   │       └── SavedAnalysis.java
│   │   │   │   │
│   │   │   │   ├── repository/                  # リポジトリ「インターフェース」（DDD/Repositoryパターン）
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── SocialAccountRepository.java
│   │   │   │   │   ├── PostRepository.java
│   │   │   │   │   ├── PostMetricsRepository.java
│   │   │   │   │   ├── HashtagRepository.java
│   │   │   │   │   ├── AnalysisResultRepository.java
│   │   │   │   │   ├── BuzzScoreRepository.java
│   │   │   │   │   ├── CompetitorStatsRepository.java
│   │   │   │   │   ├── RankingRepository.java
│   │   │   │   │   ├── ReportRepository.java
│   │   │   │   │   └── SavedAnalysisRepository.java
│   │   │   │   │
│   │   │   │   ├── service/                     # ドメインサービス（複数集約にまたがるロジック）
│   │   │   │   │   ├── buzzscore/
│   │   │   │   │   │   ├── BuzzScoreStrategy.java          # Strategyパターン: インターフェース
│   │   │   │   │   │   ├── EngagementRateStrategy.java
│   │   │   │   │   │   ├── ViewCountStrategy.java
│   │   │   │   │   │   ├── CommentRateStrategy.java
│   │   │   │   │   │   ├── FormatStrategy.java
│   │   │   │   │   │   ├── HashtagStrategy.java
│   │   │   │   │   │   ├── PostingTimeStrategy.java
│   │   │   │   │   │   ├── TextStructureStrategy.java
│   │   │   │   │   │   ├── AiInsightStrategy.java
│   │   │   │   │   │   └── BuzzScoreCalculator.java        # 各Strategyを合成するドメインサービス
│   │   │   │   │   └── competitor/
│   │   │   │   │       └── CompetitorStatsAggregator.java
│   │   │   │   │
│   │   │   │   ├── event/                        # ドメインイベント（Observerパターンの土台）
│   │   │   │   │   ├── PostAnalyzedEvent.java
│   │   │   │   │   ├── BuzzScoreCalculatedEvent.java
│   │   │   │   │   ├── ReportGeneratedEvent.java
│   │   │   │   │   └── CompetitorStatsUpdatedEvent.java
│   │   │   │   │
│   │   │   │   └── exception/
│   │   │   │       ├── DomainException.java
│   │   │   │       ├── UnsupportedPlatformException.java
│   │   │   │       └── PostNotFoundException.java
│   │   │   │
│   │   │   ├── application/                     # アプリケーション層（ユースケース）
│   │   │   │   ├── port/                         # 出力ポート（インターフェース。Infrastructureが実装）
│   │   │   │   │   ├── SocialPlatformPort.java    # = SocialPlatform 抽象インターフェース本体
│   │   │   │   │   ├── AiAnalysisPort.java
│   │   │   │   │   ├── ReportExportPort.java
│   │   │   │   │   ├── StoragePort.java
│   │   │   │   │   └── CachePort.java
│   │   │   │   │
│   │   │   │   ├── command/                      # Commandパターン: 書き込み系ユースケース入力
│   │   │   │   │   ├── AnalyzePostUrlCommand.java
│   │   │   │   │   ├── RegisterSocialAccountCommand.java
│   │   │   │   │   ├── GenerateReportCommand.java
│   │   │   │   │   ├── SaveAnalysisCommand.java
│   │   │   │   │   └── RefreshCompetitorStatsCommand.java
│   │   │   │   │
│   │   │   │   ├── query/                        # 読み取り系ユースケース入力
│   │   │   │   │   ├── SearchPostsQuery.java
│   │   │   │   │   ├── GetRankingQuery.java
│   │   │   │   │   └── GetCompetitorStatsQuery.java
│   │   │   │   │
│   │   │   │   ├── usecase/                      # ユースケース実装（CommandHandler / QueryHandler）
│   │   │   │   │   ├── post/
│   │   │   │   │   │   ├── AnalyzePostUrlUseCase.java
│   │   │   │   │   │   ├── SearchPostsUseCase.java
│   │   │   │   │   │   └── FindSimilarPostsUseCase.java
│   │   │   │   │   ├── competitor/
│   │   │   │   │   │   ├── RegisterSocialAccountUseCase.java
│   │   │   │   │   │   └── AnalyzeCompetitorUseCase.java
│   │   │   │   │   ├── ranking/
│   │   │   │   │   │   └── GetRankingUseCase.java
│   │   │   │   │   ├── report/
│   │   │   │   │   │   └── GenerateReportUseCase.java
│   │   │   │   │   ├── saved/
│   │   │   │   │   │   └── SaveAnalysisUseCase.java
│   │   │   │   │   └── auth/
│   │   │   │   │       ├── RegisterUserUseCase.java
│   │   │   │   │       └── AuthenticateUserUseCase.java
│   │   │   │   │
│   │   │   │   ├── dto/                          # ユースケースの入出力DTO
│   │   │   │   └── event/
│   │   │   │       └── listener/                 # Observerパターン: アプリケーション層のイベントリスナー
│   │   │   │           ├── ReportAutoGenerationListener.java
│   │   │   │           ├── RankingUpdateListener.java
│   │   │   │           └── CacheInvalidationListener.java
│   │   │   │
│   │   │   ├── infrastructure/                   # インフラ層（外部技術詳細の実装）
│   │   │   │   ├── persistence/
│   │   │   │   │   ├── entity/                   # JPAエンティティ（ドメインモデルとは分離）
│   │   │   │   │   ├── repository/               # Spring Data JPA リポジトリ実装
│   │   │   │   │   └── mapper/                   # JPAエンティティ ⇔ ドメインモデル変換
│   │   │   │   │
│   │   │   │   ├── platform/                     # SocialPlatform 実装群（Strategy/Factoryパターン）
│   │   │   │   │   ├── SocialPlatform.java        # インターフェース本体（application.portの実装対象）
│   │   │   │   │   ├── instagram/
│   │   │   │   │   │   ├── InstagramService.java
│   │   │   │   │   │   ├── InstagramApiClient.java
│   │   │   │   │   │   └── InstagramPostMapper.java
│   │   │   │   │   ├── tiktok/
│   │   │   │   │   │   ├── TikTokService.java
│   │   │   │   │   │   ├── TikTokApiClient.java
│   │   │   │   │   │   └── TikTokPostMapper.java
│   │   │   │   │   ├── x/
│   │   │   │   │   │   ├── XService.java
│   │   │   │   │   │   ├── XApiClient.java
│   │   │   │   │   │   └── XPostMapper.java
│   │   │   │   │   └── factory/
│   │   │   │   │       └── PlatformFactory.java   # Factoryパターン: PlatformCode -> SocialPlatform実装解決
│   │   │   │   │
│   │   │   │   ├── ai/
│   │   │   │   │   └── openai/
│   │   │   │   │       ├── OpenAiAnalysisAdapter.java   # AiAnalysisPort実装
│   │   │   │   │       ├── prompt/
│   │   │   │   │       │   ├── AnalysisPromptBuilder.java  # Builderパターン
│   │   │   │   │       │   └── PromptTemplate.java
│   │   │   │   │       └── OpenAiClient.java
│   │   │   │   │
│   │   │   │   ├── report/                       # レポート出力（Strategy + Builderパターン）
│   │   │   │   │   ├── ReportExporterFactory.java
│   │   │   │   │   ├── PdfReportExporter.java
│   │   │   │   │   ├── MarkdownReportExporter.java
│   │   │   │   │   ├── HtmlReportExporter.java
│   │   │   │   │   └── builder/
│   │   │   │   │       └── ReportContentBuilder.java
│   │   │   │   │
│   │   │   │   ├── storage/
│   │   │   │   │   └── s3/
│   │   │   │   │       └── S3StorageAdapter.java   # StoragePort実装（S3互換）
│   │   │   │   │
│   │   │   │   ├── cache/
│   │   │   │   │   └── redis/
│   │   │   │   │       └── RedisCacheAdapter.java  # CachePort実装
│   │   │   │   │
│   │   │   │   ├── security/
│   │   │   │   │   ├── jwt/
│   │   │   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   │   │   └── SecurityConfig.java
│   │   │   │   │
│   │   │   │   ├── scheduler/                    # バッチ/スケジューラ（ランキング再計算等）
│   │   │   │   │   ├── RankingRecalculationJob.java
│   │   │   │   │   └── PostMetricsRefreshJob.java
│   │   │   │   │
│   │   │   │   └── config/
│   │   │   │       ├── BeanConfig.java             # DIコンテナへのBean登録（各Strategy/Factory等）
│   │   │   │       ├── AsyncConfig.java
│   │   │   │       └── OpenApiConfig.java
│   │   │   │
│   │   │   └── presentation/                     # プレゼンテーション層（REST API）
│   │   │       ├── controller/
│   │   │       │   ├── AuthController.java
│   │   │       │   ├── PostAnalysisController.java
│   │   │       │   ├── SearchController.java
│   │   │       │   ├── SocialAccountController.java
│   │   │       │   ├── CompetitorController.java
│   │   │       │   ├── RankingController.java
│   │   │       │   ├── ReportController.java
│   │   │       │   ├── SavedAnalysisController.java
│   │   │       │   └── SettingsController.java
│   │   │       ├── dto/
│   │   │       │   ├── request/
│   │   │       │   └── response/
│   │   │       ├── mapper/
│   │   │       └── exception/
│   │   │           └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-local.yml
│   │       ├── application-prod.yml
│   │       └── db/migration/                     # Flyway マイグレーション
│   │           ├── V1__init_schema.sql
│   │           └── V2__seed_platforms.sql
│   │
│   └── test/
│       └── java/com/snsbuzz/platform/
│           ├── domain/
│           ├── application/
│           ├── infrastructure/
│           └── presentation/
└── ...
```

### レイヤー依存の原則

- `domain` は他のどの層にも依存しない（Java標準ライブラリのみに依存）。
- `application` は `domain` にのみ依存し、外部技術（DB/HTTP/AI API等）へは `port`（インターフェース）を介してのみアクセスする。
- `infrastructure` は `application.port` および `domain.repository` インターフェースを実装する（依存性逆転の原則）。
- `presentation` は `application` のユースケース（Command/Query）を呼び出し、`domain`/`infrastructure` の実装詳細には直接依存しない。

## 3. frontend/ ディレクトリ構成（Next.js App Router）

```text
frontend/
├── package.json
├── tsconfig.json
├── next.config.mjs
├── .env.local.example
├── public/
│   └── images/
└── src/
    ├── app/                                # App Router（ルーティング）
    │   ├── layout.tsx                      # ルートレイアウト
    │   ├── globals.css
    │   ├── (auth)/                         # 認証系ルートグループ
    │   │   ├── login/
    │   │   │   └── page.tsx                # SCR-001
    │   │   └── register/
    │   │       └── page.tsx                # SCR-002
    │   ├── (dashboard)/                    # ダッシュボード系ルートグループ（要認証）
    │   │   ├── layout.tsx                  # サイドナビ + ヘッダーを含む共通レイアウト
    │   │   ├── home/
    │   │   │   └── page.tsx                # SCR-003
    │   │   ├── trends/
    │   │   │   └── page.tsx                # SCR-004
    │   │   ├── competitors/
    │   │   │   ├── page.tsx                # SCR-005
    │   │   │   └── [accountId]/
    │   │   │       └── page.tsx            # SCR-006
    │   │   ├── posts/
    │   │   │   ├── page.tsx                # SCR-007（URL入力・検索）
    │   │   │   └── [postId]/
    │   │   │       └── page.tsx            # SCR-008（分析結果詳細）
    │   │   ├── reports/
    │   │   │   ├── page.tsx                # SCR-009
    │   │   │   └── [reportId]/
    │   │   │       └── page.tsx            # SCR-010
    │   │   ├── rankings/
    │   │   │   └── page.tsx                # SCR-011
    │   │   ├── saved/
    │   │   │   └── page.tsx                # SCR-012
    │   │   ├── settings/
    │   │   │   └── page.tsx                # SCR-013
    │   │   └── search/
    │   │       └── page.tsx                # SCR-014
    │   ├── api/                            # Route Handlers（BFF層：認証Cookie処理等）
    │   │   └── auth/
    │   │       └── [...nextauth]/
    │   └── error.tsx                       # SCR-015 共通エラー
    │
    ├── features/                           # 機能単位（feature-sliced）のロジック・コンポーネント
    │   ├── auth/
    │   │   ├── components/
    │   │   ├── hooks/
    │   │   └── api/
    │   ├── post-analysis/
    │   │   ├── components/
    │   │   │   ├── UrlInputForm.tsx
    │   │   │   ├── BuzzScoreGauge.tsx
    │   │   │   ├── AiAnalysisTabs.tsx
    │   │   │   ├── ImprovementSuggestionList.tsx
    │   │   │   └── SimilarPostsCarousel.tsx
    │   │   ├── hooks/
    │   │   └── api/
    │   ├── competitor-analysis/
    │   ├── ranking/
    │   ├── trends/
    │   ├── report/
    │   ├── saved-analysis/
    │   └── settings/
    │
    ├── components/                         # 共通UIコンポーネント
    │   ├── ui/                             # ボタン・入力・モーダル等の基礎コンポーネント
    │   ├── layout/
    │   │   ├── SideNavigation.tsx
    │   │   ├── Header.tsx
    │   │   └── GlobalSearchBar.tsx
    │   └── charts/
    │       ├── EngagementLineChart.tsx
    │       ├── PostingTimeHeatmap.tsx
    │       └── GenreDistributionChart.tsx
    │
    ├── lib/
    │   ├── api-client.ts                   # バックエンドAPIクライアント（fetchラッパー）
    │   ├── auth.ts                         # JWTトークン管理
    │   └── utils.ts
    │
    ├── stores/                             # クライアント状態管理（例: Zustand）
    │   ├── authStore.ts
    │   └── uiStore.ts
    │
    ├── types/                              # API DTOに対応するTypeScript型
    │   ├── post.ts
    │   ├── analysis.ts
    │   ├── buzzScore.ts
    │   ├── competitor.ts
    │   ├── ranking.ts
    │   ├── report.ts
    │   └── user.ts
    │
    └── styles/
        └── theme.ts
```

### フロントエンド構成の設計意図

- App Router の `(auth)` / `(dashboard)` ルートグループにより、認証要否でレイアウトとミドルウェアを分離します。
- `features/` はドメイン機能ごとにコンポーネント・フック・API呼び出しをまとめる Feature-Sliced 的構成とし、`03_screens.md` の画面と1対1で対応づけやすくしています。
- `types/` はバックエンドの `presentation.dto.response` と対応する型を持ち、`10_api_design.md` のレスポンス例と型を一致させます。

## 4. インフラ構成（Docker Compose）

```text
docker-compose.yml
├── services:
│   ├── frontend        # Next.js (build: infra/docker/frontend.Dockerfile)
│   ├── backend          # Spring Boot (build: infra/docker/backend.Dockerfile)
│   ├── postgres         # PostgreSQL 16
│   ├── redis             # Redis 7
│   ├── minio              # S3互換オブジェクトストレージ（開発/初期運用用）
│   └── nginx (optional)  # リバースプロキシ / TLS終端
└── volumes:
    ├── postgres_data
    ├── redis_data
    └── minio_data
```

将来 AWS/Cloud Run/Kubernetes へ移行する際は、`backend`/`frontend` のコンテナイメージをそのまま流用し、`postgres` → RDS、`redis` → ElastiCache、`minio` → S3 に置き換える想定です（12-factor app 原則に基づき、接続先はすべて環境変数で切り替え可能）。
