# 06. ディレクトリ構成

本ドキュメントは、実際に実装されたリポジトリのディレクトリ構成を示します。バックエンドはクリーンアーキテクチャ + DDD に基づく `backend/`（Java 21 / Spring Boot / Gradle）、フロントエンドは Next.js App Router に基づく `frontend/`（React / TypeScript）です。

## 1. リポジトリ全体構成

```text
.
├── backend/                     # Spring Boot バックエンド（Java 21, Gradle）
├── frontend/                    # Next.js フロントエンド（React, TypeScript）
├── docs/                        # 設計ドキュメント（本ディレクトリ）
├── docker-compose.yml           # ローカル/初期運用向け Compose 定義（postgres/redis/minio/backend/frontend）
├── .gitignore
└── README.md                    # セットアップ手順・デプロイ手順
```

## 2. backend/ ディレクトリ構成（クリーンアーキテクチャ）

パッケージルートは `com.buzzanalysis` とし、`domain` → `application` → `infrastructure` / `presentation` の依存方向を厳守します（依存性逆転の原則：`domain`/`application` は外部フレームワークに依存しません）。

```text
backend/
├── build.gradle
├── settings.gradle
├── Dockerfile
├── gradle/wrapper/               # ※オフライン環境のため未生成。README参照
└── src/
    ├── main/
    │   ├── java/com/buzzanalysis/
    │   │   ├── BuzzAnalysisApplication.java        # エントリポイント
    │   │   │
    │   │   ├── domain/                              # ドメイン層（フレームワーク非依存）
    │   │   │   ├── account/                         # SocialAccount, SocialAccountRepository(IF)
    │   │   │   ├── analysis/                         # AnalysisResult, AnalysisCompletedEvent(Observer)
    │   │   │   ├── competitor/                       # CompetitorStats
    │   │   │   ├── platform/                         # SocialPlatform(IF), PlatformFactory(IF), Platform enum
    │   │   │   ├── post/                              # Post, PostType, PostSearchCriteria
    │   │   │   ├── ranking/                           # Ranking, RankingType
    │   │   │   ├── report/                             # Report, ReportFormat
    │   │   │   ├── savedanalysis/                      # SavedAnalysis
    │   │   │   ├── score/
    │   │   │   │   ├── BuzzScoreCalculator.java        # Strategy(Context)
    │   │   │   │   └── strategy/                        # BuzzScoreStrategy実装群(8種)
    │   │   │   ├── user/                                # User, Role
    │   │   │   └── common/exception/                    # ドメイン例外
    │   │   │
    │   │   ├── application/                          # アプリケーション層（ユースケース）
    │   │   │   ├── auth/                               # AuthApplicationService, TokenProvider(port)
    │   │   │   ├── post/                                # PostAnalysisApplicationService, PostSearchApplicationService
    │   │   │   ├── competitor/                          # CompetitorAnalysisApplicationService
    │   │   │   ├── ranking/                              # RankingApplicationService
    │   │   │   ├── report/
    │   │   │   │   └── command/                          # GenerateReportCommand(Command)
    │   │   │   ├── savedanalysis/                        # SavedAnalysisApplicationService
    │   │   │   └── event/                                 # AnalysisCompletedEventListener(Observer)
    │   │   │
    │   │   ├── infrastructure/                        # インフラ層（フレームワーク依存の実装）
    │   │   │   ├── persistence/
    │   │   │   │   ├── entity/                           # JPA Entity
    │   │   │   │   ├── repository/                       # Spring Data JPA インターフェース
    │   │   │   │   ├── adapter/                          # domainリポジトリIFの実装(Repositoryパターン)
    │   │   │   │   ├── mapper/                           # Entity ⇔ ドメインモデル変換
    │   │   │   │   └── converter/                        # JSON等のAttributeConverter
    │   │   │   ├── external/
    │   │   │   │   ├── platform/                          # PlatformFactoryImpl(Factory)
    │   │   │   │   │   ├── instagram/                      # InstagramService
    │   │   │   │   │   ├── tiktok/                          # TikTokService
    │   │   │   │   │   └── x/                                # XService
    │   │   │   │   └── openai/                             # OpenAI APIクライアント
    │   │   │   ├── report/                                 # Pdf/Markdown/HtmlReportCommand実装
    │   │   │   ├── cache/                                  # Redis設定
    │   │   │   ├── storage/                                # S3互換ストレージ連携
    │   │   │   ├── security/                               # JWT/Spring Security設定
    │   │   │   └── config/                                 # Bean定義, OpenAPI設定等
    │   │   │
    │   │   └── presentation/                           # プレゼンテーション層
    │   │       ├── controller/                            # REST Controller
    │   │       ├── dto/ , dto/request/                     # リクエスト/レスポンスDTO
    │   │       └── exception/                              # GlobalExceptionHandler
    │   └── resources/
    │       ├── application.yml                          # default/docker プロファイル
    │       └── db/migration/
    │           ├── V1__init_schema.sql                    # テーブル/インデックス/外部キー
    │           └── V2__sample_data.sql                    # サンプルデータ
    └── test/
        └── java/com/buzzanalysis/
            ├── domain/score/strategy/                     # Strategy単体テスト
            ├── application/auth/, application/post/         # Mockitoを用いた単体テスト
            ├── infrastructure/persistence/                  # Testcontainersを用いた統合テスト
            └── presentation/controller/                      # MockMvcを用いたAPIテスト
```

## 3. frontend/ ディレクトリ構成（Next.js App Router）

```text
frontend/
├── package.json
├── tsconfig.json
├── next.config.ts                # output: "standalone"
├── tailwind.config.ts
├── middleware.ts                 # JWT Cookieによる未認証リダイレクト
├── Dockerfile
├── .env.example
└── app/
    ├── layout.tsx / providers.tsx
    ├── (auth)/
    │   ├── login/page.tsx
    │   └── register/page.tsx
    └── (dashboard)/
        ├── layout.tsx             # サイドバー(8画面) + ヘッダー
        ├── page.tsx                # ホーム
        ├── trend/page.tsx           # トレンド
        ├── competitors/page.tsx      # 競合分析
        ├── posts/analyze/page.tsx     # 投稿URL分析
        ├── reports/page.tsx            # AIレポート
        ├── rankings/page.tsx            # ランキング
        ├── saved/page.tsx                # 保存済み分析
        └── settings/page.tsx              # 設定

components/
├── layout/                        # サイドバー・ヘッダー・シェル
├── dashboard/                     # KPIカード, 投稿カード, 分析結果セクション群
├── charts/                        # recharts / ヒートマップ
├── auth/                          # ログイン・登録フォーム
└── ui/                            # 汎用UIコンポーネント(Button, Card, Tabs等)

lib/
├── api-client.ts                  # fetchラッパー(JWT付与, エラー正規化)
├── api/                           # リソース別APIクライアント関数
├── hooks/                         # React Query hooks
├── auth/                          # トークン管理, AuthContext
└── types/                         # バックエンドDTOに対応する型定義
```

## 4. 設計上のポイント

- バックエンドは **domain 層が最も内側**にあり、Spring/JPA/Redis/S3など外部フレームワークへの依存を一切持ちません。`infrastructure` 層が `domain` のリポジトリインターフェースを実装することで依存性逆転の原則(DIP)を満たしています。
- 新しいSNS（YouTube, Pinterest, Threads等）を追加する場合は `infrastructure/external/platform/` 配下に新しい `SocialPlatform` 実装クラスを追加し、`Platform` enum と `PlatformFactoryImpl` に登録するだけで拡張できます。
- フロントエンドは `lib/api/` にバックエンドのエンドポイントと1対1対応するクライアント関数を配置し、`lib/hooks/` でReact Query化することで、画面コンポーネントからは型安全なフックを呼ぶだけでよい構成にしています。
