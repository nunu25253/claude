# SNS AIバズ分析プラットフォーム

Instagram / TikTok / X（将来的に YouTube, Pinterest, Threads へ拡張予定）の **公開投稿のみ** を対象に、AIが「なぜバズったか」「どのような投稿を作れば伸びるか」を分析・提案するプラットフォームです。

> **重要な設計方針**
> - 利用規約を遵守し、**公式APIを優先**して利用します（スクレイピング前提の実装は行いません）。
> - インプレッション・リーチ・保存数など**非公開データは取得対象外**です。取得するのは公開されている投稿URL、投稿ID、投稿日時、投稿者、キャプション、ハッシュタグ、いいね数、コメント数、再生数（公開されている場合）、シェア数（取得可能な場合）、動画時間、画像枚数、投稿種別などです。
> - `SocialPlatform` 抽象インターフェースにより、将来のSNS追加を容易にする設計にしています。

## ドキュメント

詳細設計は [`docs/`](./docs) ディレクトリを参照してください。

| # | ドキュメント |
|---|---|
| 1 | [システム要件定義](./docs/01_requirements.md) |
| 2 | [機能一覧](./docs/02_features.md) |
| 3 | [画面一覧](./docs/03_screens.md) |
| 4 | [画面遷移図](./docs/04_screen_flow.md) |
| 5 | [ER図](./docs/05_er_diagram.md) |
| 6 | [ディレクトリ構成](./docs/06_directory_structure.md) |
| 7 | [クラス図](./docs/07_class_diagram.md) |
| 8 | [シーケンス図](./docs/08_sequence_diagram.md) |
| 9 | [DB設計](./docs/09_db_design.md) |
| 10 | [API設計](./docs/10_api_design.md) / [OpenAPI仕様](./docs/openapi.yaml) |

## 技術スタック

| 領域 | 技術 |
|---|---|
| Frontend | React, Next.js (App Router), TypeScript, Tailwind CSS, React Query |
| Backend | Java 21, Spring Boot 3, Spring Data JPA, Spring Security (JWT) |
| Database | PostgreSQL, Flyway |
| Cache | Redis |
| Storage | Amazon S3互換オブジェクトストレージ（ローカルはMinIO） |
| AI | OpenAI API |
| Build | Gradle (Backend) / npm (Frontend) |
| Infra | Docker, Docker Compose（将来的にAWS / Cloud Run / Kubernetesへ移行可能な設計） |

アーキテクチャは **クリーンアーキテクチャ + DDD** を採用し、Repository / Factory / Strategy / Builder / Observer / Command の各デザインパターンを適用しています。詳細は [クラス図](./docs/07_class_diagram.md) を参照してください。

## ディレクトリ構成

```text
.
├── backend/           # Spring Boot バックエンド (Java 21)
├── frontend/          # Next.js フロントエンド (TypeScript)
├── docs/              # 設計ドキュメント一式
├── docker-compose.yml # ローカル環境一式 (postgres/redis/minio/backend/frontend)
└── README.md
```

---

## セットアップ手順

### 前提条件

- Docker / Docker Compose v2 以降（推奨。最も簡単に一式を起動できます）
- ローカルで個別に動かす場合: Java 21, Node.js 20+, Gradle 8.x, PostgreSQL 16, Redis 7

### 1. リポジトリ取得と環境変数の設定

```bash
git clone <このリポジトリのURL>
cd claude
cp .env.example .env
```

`docker-compose.yml` は環境変数が未設定でもすべてデフォルト値で動作しますが、AI分析やSNS連携を実際に動かす場合は以下を `.env` またはシェル環境に設定してください。

```bash
# OpenAI (AI分析に必須。未設定の場合はルールベースのフォールバック分析結果が返ります)
OPENAI_API_KEY=sk-xxxxxxxx
OPENAI_MODEL=gpt-4o-mini

# 各SNS公式APIの認証情報（未設定の場合はスタブデータにフォールバックします）
INSTAGRAM_ACCESS_TOKEN=
TIKTOK_ACCESS_TOKEN=
X_BEARER_TOKEN=

# JWT署名鍵（本番相当で動かす場合は必ず変更してください）
JWT_SECRET=change-this-secret-in-production-please-0123456789abcdef
```

### 2. Docker Composeで一式起動（推奨）

```bash
docker compose up -d --build
```

起動するサービス:

| サービス | URL | 用途 |
|---|---|---|
| frontend | http://localhost:3000 | ダッシュボードUI |
| backend | http://localhost:8080 | REST API |
| backend Swagger UI | http://localhost:8080/swagger-ui.html | API仕様確認・動作確認 |
| postgres | localhost:5432 | DB（Flywayにより自動でスキーマ作成＋サンプルデータ投入） |
| redis | localhost:6379 | キャッシュ |
| MinIO コンソール | http://localhost:9001 | S3互換ストレージ管理画面（初期ユーザー: `.env`の`S3_ACCESS_KEY`/`S3_SECRET_KEY`） |

起動確認:

```bash
docker compose ps
curl http://localhost:8080/actuator/health
```

停止:

```bash
docker compose down        # コンテナのみ停止（データは保持）
docker compose down -v     # ボリュームも含めて完全に削除
```

### 3. ローカル（非Docker）でバックエンドを動かす場合

`backend/gradle/wrapper/` は本開発環境がオフラインのため `gradlew` の生成物（`gradle-wrapper.jar`）を同梱していません。ローカルにインターネット接続がある場合は以下で一度生成してください（以降はコミットして通常のプロジェクトと同様に `./gradlew` を使えます）。

```bash
cd backend
gradle wrapper --gradle-version 8.10   # ローカルにGradleが必要
```

Gradleが未インストールの場合は [SDKMAN](https://sdkman.io/) 等で導入するか、Dockerビルド（`docker compose up backend`）を利用してください。

起動（要 PostgreSQL / Redis がローカルまたは別途起動済みであること）:

```bash
cd backend
export DB_HOST=localhost DB_USERNAME=buzz_user DB_PASSWORD=buzz_password
./gradlew bootRun   # もしくは `gradle bootRun`
```

テスト実行:

```bash
./gradlew test
```

※ `Testcontainers` を使ったリポジトリ統合テストはDockerデーモンが必要です。Dockerが使えない環境では該当テストはスキップされます。

### 4. ローカル（非Docker）でフロントエンドを動かす場合

```bash
cd frontend
cp .env.example .env.local   # NEXT_PUBLIC_API_BASE_URL 等を必要に応じて変更
npm install
npm run dev
```

http://localhost:3000 で確認できます。

---

## サンプルデータ / 動作確認

Flyway の `V2__sample_data.sql`（`backend/src/main/resources/db/migration/`）により、起動時に投稿・アカウント・分析結果などのサンプルデータが自動投入されます。ログイン画面からサンプルユーザーでログインするか、`/api/v1/auth/register` から新規登録して各画面を確認してください。

投稿URL分析のAPI動作確認例:

```bash
curl -X POST http://localhost:8080/api/v1/posts/analyze \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <ログインで取得したJWT>" \
  -d '{"url": "https://www.instagram.com/reel/xxxxxxxx/"}'
```

---

## 定期データ取得バッチ（自動化）

競合分析・ランキングを常に最新の状態に保つため、追跡対象アカウント（`social_accounts.tracking_enabled = true`。投稿URL分析や競合分析で登録したアカウントはデフォルトで追跡対象になります）の最新公開投稿を定期的に自動取得するバッチ機能があります。

- 取得元は各SNSの`SocialPlatform`実装（公式API、未設定時はスタブ）で、**取得するのは公開データのみ**です。
- 取得した投稿のBuzzScoreは自動で再計算されます（コスト抑制のため、このバッチではOpenAIによるAI再分析は行いません。AI分析はユーザーが「投稿URL分析」を実行した場合のみ行われます）。
- 急上昇(48時間)/週間(7日)/月間(30日)の各ランキングを、全体版・SNS別版で自動的に再構築します。
- 完了後、競合分析・ランキングのRedisキャッシュを自動で無効化します。

**有効化方法**（デフォルトはOFF。`.env` で設定):

```bash
BATCH_SYNC_ENABLED=true
BATCH_SYNC_CRON=0 0 * * * *   # 毎時0分に実行（cron式）
BATCH_SYNC_POST_LIMIT=20      # 1アカウントあたりの取得件数
```

cronを待たずに即時実行したい場合は、以下のAPIを呼び出してください（有効化していなくても手動実行は可能です）。

```bash
curl -X POST http://localhost:8080/api/v1/sync/run \
  -H "Authorization: Bearer <ログインで取得したJWT>"
```

レスポンスには処理したアカウント数・同期した投稿数・更新したランキング件数・アカウント単位のエラー一覧が含まれます。詳細は [API設計](./docs/10_api_design.md)（「定期データ取得バッチ」節）を参照してください。

---

## デプロイ手順

### ローカル/検証環境（Docker Compose）

前述の `docker compose up -d --build` で単一ホスト上に一式をデプロイできます。単一VM上での検証・小規模運用にはこの構成で十分です。

### 本番運用（AWS / Cloud Run / Kubernetes への移行）

本プロジェクトは以下の理由からクラウド移行が容易な構成にしています。

- **ステートレスなアプリケーション層**: `backend` / `frontend` はどちらもコンテナイメージのみで完結し、状態は PostgreSQL / Redis / S3互換ストレージに集約しています。
- **設定の外部化**: DB接続・Redis・S3・JWT・OpenAI・各SNS APIキーはすべて環境変数で注入する構成（`application.yml` の `${VAR:default}` 記法）のため、実行環境を変えてもコード変更は不要です。

想定する移行パス:

1. **AWS (ECS Fargate + RDS + ElastiCache + S3)**
   - `backend`/`frontend` の Docker イメージを Amazon ECR にプッシュし、ECS Fargate サービスとしてデプロイ。
   - PostgreSQL は Amazon RDS for PostgreSQL、Redis は Amazon ElastiCache for Redis に置き換え、MinIOの代わりに実際のAmazon S3バケットを使用（`S3_ENDPOINT` を空にすれば標準のAWS S3エンドポイントが使われます）。
   - ALB経由でfrontend/backendを公開し、Route53 + ACMでカスタムドメイン・TLSを設定。
   - シークレットは AWS Secrets Manager / Systems Manager Parameter Store で管理し、ECSタスク定義から注入。

2. **Google Cloud Run**
   - `backend`/`frontend` イメージを Artifact Registry にプッシュし、Cloud Run サービスとしてデプロイ（両者ともステートレスコンテナのためCloud Runと相性が良い構成です）。
   - PostgreSQLは Cloud SQL for PostgreSQL（Cloud SQL Auth Proxy経由で接続）、Redisは Memorystore for Redis、ストレージは Google Cloud Storage（S3互換モードもしくはアダプタ実装への差し替え）を使用。
   - 環境変数・シークレットは Secret Manager から Cloud Run サービスに注入。

3. **Kubernetes（EKS / GKE等）**
   - `backend`/`frontend` の Deployment + Service マニフェスト（Ingressで外部公開）を作成し、`docker-compose.yml` の環境変数定義を ConfigMap / Secret に変換。
   - PostgreSQL / Redis はマネージドサービスを利用するか、Kubernetes Operator（例: CloudNativePG, Redis Operator）で運用。
   - HPA（Horizontal Pod Autoscaler）でbackend/frontendのオートスケールを設定。

いずれの移行パスでも、アプリケーションコード自体の変更は不要で、環境変数とインフラ定義（IaC）の追加のみで対応できる設計です。

---

## テスト

```bash
# バックエンド
cd backend && ./gradlew test

# フロントエンド
cd frontend && npm run build && npx tsc --noEmit && npx eslint .
```

## ライセンス・注意事項

本プロジェクトは公開情報のみを扱う分析ツールとして設計されています。実際に各SNSの公式APIを利用する際は、各社の開発者向け利用規約・レート制限・データ利用ポリシーを必ず確認し、遵守してください。
