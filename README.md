# SNS AIバズ分析プラットフォーム（AIマーケティングOS）

[![CI](https://github.com/nunu25253/claude/actions/workflows/ci.yml/badge.svg)](https://github.com/nunu25253/claude/actions/workflows/ci.yml)

Instagram / TikTok / X（将来的に YouTube, Pinterest, Threads へ拡張予定）の **公開投稿のみ** を対象に、AIが「なぜバズったか」「どのような投稿を作れば伸びるか」を分析・提案するプラットフォームです。バズ分析基盤の上に、投稿データの正規化・前処理からAIによる企画・台本・カルーセル生成、投稿評価、トレンド分析、RAGまでを備えた「AIマーケティングOS」として拡張されています（後述）。

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

## AIマーケティングOS拡張機能（Phase1-17）

バズ分析基盤（要件定義〜デプロイ手順の初期18成果物）の完成後、以下の17フェーズで「SNS AIマーケティングOS」へ拡張しました。各フェーズは**設計→レビュー→実装→テスト**のサイクルで、フェーズ着手前に必ずSNSのAPI/ToS制約を踏まえた実現可能性のセルフレビューを行っています（各フェーズのドキュメントに記載）。

| Phase | 機能 | 概要 | 設計ドキュメント |
|---|---|---|---|
| 1 | 正規化 | プラットフォーム横断の投稿データ正規化（Strategyパターン） | [phase1](./docs/phases/phase1_normalization.md) |
| 2 | 前処理 | AI分析用テキストクリーニング・ハッシュタグ/メンション抽出等 | [phase2](./docs/phases/phase2_preprocessing.md) |
| 3 | Embedding生成 | OpenAI Embeddings + pgvectorによるベクトル化 | [phase3](./docs/phases/phase3_embeddings.md) |
| 4 | 意味検索 | キーワードの意味的な近さで投稿を検索 | [phase4](./docs/phases/phase4_semantic_search.md) |
| 5 | 投稿分析AI拡張 | ジャンル/サブジャンル/投稿目的/強み弱み等の分析項目拡張 | [phase5](./docs/phases/phase5_post_analysis.md) |
| 6 | ユーザー条件分析 | 検索条件と投稿の一致率算出 | [phase6](./docs/phases/phase6_condition_matching.md) |
| 7 | ランキングAI | 一致率+BuzzScore等の重み付き総合ランキング | [phase7](./docs/phases/phase7_ranking.md) |
| 8 | 共通点分析 | 投稿群の統計的共通項目＋AIによる共通パターン抽出 | [phase8](./docs/phases/phase8_commonality_analysis.md) |
| 9 | 競合分析拡張 | 平均再生数/投稿形式分布/ジャンル分布＋AI差分説明 | [phase9](./docs/phases/phase9_competitor_analysis.md) |
| 10 | 企画生成AI | 共通点分析結果を元に投稿企画（既定20件）を生成 | [phase10](./docs/phases/phase10_proposal_generation.md) |
| 11 | 台本生成AI | 30/60/90秒動画のナレーション・テロップ・カット構成生成 | [phase11](./docs/phases/phase11_script_generation.md) |
| 12 | カルーセル生成AI | Instagramカルーセル（2〜8ページ）生成 | [phase12](./docs/phases/phase12_carousel_generation.md) |
| 13 | 画像生成プロンプト | 画像生成AI向けプロンプト文字列の作成（画像生成自体は行わない） | [phase13](./docs/phases/phase13_image_prompt_generation.md) |
| 14 | 投稿評価AI | 一致率・改善提案・予測投稿スコアの算出 | [phase14](./docs/phases/phase14_post_evaluation.md) |
| 15 | トレンド分析 | 急上昇ハッシュタグ/ジャンル/コンテンツ形式の検出（日次バッチ対応） | [phase15](./docs/phases/phase15_trend_analysis.md) |
| 16 | RAG | 過去の分析結果等を索引化し根拠付きでAIが回答 | [phase16](./docs/phases/phase16_rag.md) |
| 17 | ダッシュボード | 「AI企画」「投稿評価」画面を追加し要求仕様の9画面が完成 | [phase17](./docs/phases/phase17_dashboard.md) |

すべてのフェーズを通じた設計上の一貫方針:

- **未計測データと0を混同しない**: 実測できない指標（Phase1の非公開メトリクス、Phase6/14の比較基準がない一致率等）は`null`として扱い、便宜的な数値で埋めない。
- **既存機能の再利用を優先し、新規重複を避ける**: 新フェーズが既存の集約・APIと重なる場合は拡張を優先し、ドメインが明確に異なる場合のみ新規集約を作る（判断根拠は各フェーズドキュメントに明記）。
- **AIの出力を信頼せず検証・補正する**: 台本のカット秒数（Phase11）、カルーセルのページ数・役割（Phase12）等、構造的な制約はコード側で検証し、AIには内容生成のみを任せる。
- **コストを意識したAI呼び出し**: サンプリング・要約・差分スキップ（Phase3/8/9）、要素ごとの部分フォールバック（Phase13）など。

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
# OpenAI呼び出しのリトライ・サーキットブレーカー設定(省略可、デフォルト値で動作)。
# 障害時に失敗が続くと一定時間サーキットブレーカーがOPENになり、以降の呼び出しは
# 実際にAPIへアクセスせず即座にルールベースのフォールバックへ切り替わる。
# OPENAI_RETRY_MAX_ATTEMPTS=3
# OPENAI_CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD=50
# OPENAI_CIRCUIT_BREAKER_WAIT_DURATION_IN_OPEN_STATE=10s

# 各SNS公式APIの認証情報（未設定の場合はスタブデータにフォールバックします。取得方法・制約は
# 「SNS公式API連携」セクションを参照）
INSTAGRAM_ACCESS_TOKEN=
INSTAGRAM_BUSINESS_ACCOUNT_ID=
TIKTOK_ACCESS_TOKEN=
X_BEARER_TOKEN=

# 課金プラン(FREE/PRO、No.11)。決済代行事業者と未契約の場合は既定値(mock)のままでよく、
# 常に成功する疑似決済でPROプランへのアップグレード・解約が動作します。
# PAYMENT_GATEWAY_PROVIDER=mock
# 本番でGMOペイメントゲートウェイ(GMO-PG)と連携する場合は下記を設定してください。
# 実際のエンドポイント・パラメータ名は契約時にGMOから提供される技術仕様書で必ず確認してください
# (backend/.../infrastructure/payment/GmoPaymentGatewayAdapter.java のJavadoc参照)。
# PAYMENT_GATEWAY_PROVIDER=gmo
# GMO_PAYMENT_SHOP_ID=
# GMO_PAYMENT_SHOP_PASS=
# GMO_PAYMENT_PRO_PLAN_MONTHLY_AMOUNT=4980
# PRO契約の月次更新課金バッチ(決済処理を伴うため既定でOFF)。
# BATCH_BILLING_RENEWAL_ENABLED=true
# QUOTA_DAILY_OPENAI_CALLS_FREE=50
# QUOTA_DAILY_OPENAI_CALLS_PRO=500

# JWT署名鍵（本番相当で動かす場合は必ず変更してください）
JWT_SECRET=change-this-secret-in-production-please-0123456789abcdef

# storage.provider=local（既定）時、AIレポートのダウンロードURLに署名するHMAC鍵。
# JWT_SECRETと同様、本番相当で動かす場合は必ず変更してください
# （変更しないと、既定値を知っている第三者がダウンロードURLを偽造できてしまいます）。
STORAGE_LOCAL_SIGNING_SECRET=change-this-secret-in-production-please-0123456789abcdef
# ダウンロードURLの有効期限（分）。既定60分。
# STORAGE_LOCAL_PRESIGNED_URL_EXPIRATION_MINUTES=60
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
| MailHog | http://localhost:8025 | 送信メール確認用のテストSMTPキャッチャー(パスワードリセット・メール確認・しきい値アラート等) |
| Zipkin | http://localhost:9411 | 分散トレーシングUI。リクエスト単位でHTTP呼び出し・OpenAI/SNS公式APIへの外部呼び出しを1画面で追跡できる |

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

#### バックエンドAPIの型定義について

`frontend/lib/types/generated/api.ts` はバックエンド起動中に以下のコマンドで生成される、
springdoc-openapiが実装(Controller/DTO)から自動生成したOpenAPI契約由来のTypeScript型です。
`docs/openapi.yaml`の手動同期や型の手書きコピーによるフロント/バックエンドの契約ドリフトを防ぐため、
バックエンドのリクエスト/レスポンスDTOを変更した場合は必ず再生成してコミットしてください
(CIで`generate:api-types`の再実行結果と差分がないか検証しています)。

```bash
# バックエンドを起動した状態で実行
cd frontend
npm run generate:api-types
```

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

## SNS公式API連携

各SNS向けの `SocialPlatform` 実装（`InstagramService`/`TikTokService`/`XService`）は、対応する認証情報が未設定の場合は開発・デモ用のスタブ（モック）データにフォールバックします。実際のSNSから公開データを取得するには、以下の手順で認証情報を取得し `.env` に設定してください。**いずれも「非公開データ（インプレッション・リーチ・保存数等）」は取得しません／取得できません。**

各プラットフォームで「できること」はAPI仕様上大きく異なります。

| プラットフォーム | 単一投稿の分析(`fetchPost`) | アカウントの投稿一覧取得(`fetchRecentPosts`) | 任意の第三者アカウントを閲覧可能か |
|---|---|---|---|
| Instagram | ⚠️ 対象アカウントの直近50件以内のみ | ✅ (Business Discovery) | ✅ 可能（対象も自社もビジネス/クリエイターアカウントである必要あり） |
| TikTok | ⚠️ oEmbedのみ（エンゲージメント指標なし） | ✅ ただし**自分自身のアカウントのみ** | ❌ 不可（TikTok公式APIの仕様上の制約） |
| X (旧Twitter) | ✅ | ✅ | ✅ 可能（ただし無料プランでは読み取り系エンドポイント自体が使えない） |

### Instagram（Graph API / Business Discovery）

1. [Meta for Developers](https://developers.facebook.com/) でアプリを作成（種類: 「ビジネス」）。
2. 自社のInstagramアカウントを「ビジネスアカウント」または「クリエイターアカウント」に変更し、Facebookページと連携する。
3. アプリにFacebookページ・Instagramアカウントを接続し、`instagram_basic` 権限を持つアクセストークンを発行する（長期トークンへの交換を推奨）。
4. 連携したInstagramアカウントの「IG User ID」を控える（Graph API Explorerや `GET /me/accounts` 等で取得可能）。
5. `.env` に設定:
   ```bash
   INSTAGRAM_ACCESS_TOKEN=EAAxxxxxxxxxx
   INSTAGRAM_BUSINESS_ACCOUNT_ID=1789xxxxxxxxxxx
   ```

**制約:** Business Discoveryは「自社アカウントのトークンで“他の”ビジネス/クリエイターアカウントの公開データを閲覧する」仕組みのため、閲覧対象のアカウントもビジネス/クリエイターアカウントである必要があります（個人アカウントは非対応）。また、投稿URL単体からの分析（`fetchPost`）は対象アカウントの直近50件の投稿からしか検索できません（Instagram API自体に「投稿URLから直接1件取得する」手段が用意されていないための制約です）。

### TikTok（Display API v2 / Login Kit）

1. [TikTok for Developers](https://developers.tiktok.com/) でアプリを登録し、Client Key/Secretを取得。
2. Login Kitの認可コードフロー（ブラウザ経由のOAuth 2.0）を一度実行し、`user.info.basic`・`video.list` スコープのアクセストークンを取得する（このアプリには認可コールバック画面は未実装のため、初回のトークン取得はPostman等で手動実施するか、別途OAuthコールバック実装を追加する必要があります）。
3. `.env` に設定:
   ```bash
   TIKTOK_ACCESS_TOKEN=act.xxxxxxxxxx
   ```

**制約:** TikTokの公開APIはOAuthで認可した**本人のアカウントのデータしか取得できません**（Instagramのような第三者アカウント閲覧の仕組みはありません）。競合TikTokアカウントの投稿一覧を自動取得することは公式APIの仕様上できません。単一投稿URLの分析（`fetchPost`）はトークン不要のoEmbed（`https://www.tiktok.com/oembed`）を使い、投稿者名・タイトル・サムネイルのみ取得します（いいね数等のエンゲージメント指標はoEmbedには含まれません）。

### X（旧Twitter API v2）

1. [developer.x.com](https://developer.x.com/) で開発者アカウントを申請し、Project + Appを作成。
2. App-onlyのBearer Tokenを発行する。
3. `.env` に設定:
   ```bash
   X_BEARER_TOKEN=AAAAAAAAAAAAAAAAAAAAA...
   ```

**制約:** ユーザー検索・投稿検索・タイムライン取得などの読み取り系エンドポイントは、Xの無料プラン（Free tier）では利用できません。最低でもBasicプラン以上（有料）の契約が必要です（詳細はXの公式料金ページを参照してください）。契約さえあれば、Instagramと同様に任意の公開アカウント・投稿をApp-only認証で閲覧できます。

なお、Free tierのトークンを設定した場合や、その他の理由でAPI呼び出しが失敗した場合（Instagram/TikTokも同様）は、エラー画面を出さずに開発・デモ用のスタブ（モック）データへ自動的にフォールバックします（サーバーログには警告として原因が記録されます）。そのため、有料プランに入らなくてもアプリ自体の動作確認は可能です。

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

## CI/CD（GitHub Actions）

`.github/workflows/ci.yml` により、`main`ブランチへのpushおよびすべてのプルリクエストで以下を自動実行します。

| ジョブ | 内容 |
|---|---|
| `backend` | `gradle test` / `gradle build`（JDK 21）。Testcontainersを使ったリポジトリ統合テストも、GitHub Actionsランナーには標準でDockerが利用可能なため実行されます。 |
| `frontend` | `npm run typecheck` / `npm run lint` / `npm run build`（Node.js 20） |
| `e2e` | Docker Composeで一式起動し、主要導線のPlaywrightテストに加えて`frontend/lib/types/generated/api.ts`がバックエンドの実際のOpenAPI契約と一致しているか(契約ドリフトの検出)を検証 |
| `validate-configs` | `docs/openapi.yaml` のOpenAPI仕様検証、`docker-compose.yml` の構文検証 |

> **補足**: 本開発環境（サンドボックス）はネットワーク制限により `gradlew`（Gradle Wrapper）を生成・コミットできていません（`services.gradle.org` に到達不可のため）。GitHub Actions上では`gradle/actions/setup-gradle`でGradle本体を直接セットアップして`gradle`コマンドを実行しています。インターネット接続のある環境であれば `cd backend && gradle wrapper --gradle-version 8.14.3` でWrapperを生成しコミットすることで、以降は通常どおり`./gradlew`が使えます。

## ライセンス・注意事項

本プロジェクトは公開情報のみを扱う分析ツールとして設計されています。実際に各SNSの公式APIを利用する際は、各社の開発者向け利用規約・レート制限・データ利用ポリシーを必ず確認し、遵守してください。
