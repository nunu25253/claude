# 10. API設計

本ドキュメントは本システムのREST API設計です。OpenAPI 3.0形式の詳細仕様は `docs/openapi.yaml` を参照してください。エンティティ名・用語は `01_requirements.md` の用語定義、テーブル定義は `09_db_design.md` に準拠します。

## 1. 基本方針

- ベースURL: `https://api.example.com/api/v1`
- 通信はすべてHTTPS、JSON形式（`Content-Type: application/json; charset=utf-8`）
- 認証: `Authorization: Bearer <JWTアクセストークン>` ヘッダーによるBearer認証
- IDはすべてUUID文字列
- 日時はISO 8601（`YYYY-MM-DDTHH:mm:ssZ`、UTC）
- 命名規則: パスは小文字ケバブケース、JSONフィールドはキャメルケース

## 2. ページング・検索・フィルタ・ソートのクエリパラメータ規約

一覧取得系APIは以下のクエリパラメータ規約に従います。

| パラメータ | 型 | デフォルト | 説明 |
|-----------|----|-----------|------|
| `page` | integer | `0` | ページ番号（0始まり） |
| `size` | integer | `20`（最大 `100`） | 1ページあたりの件数 |
| `sort` | string | エンドポイントごとの既定値 | `フィールド名,asc\|desc` 形式。複数指定時はカンマ区切りで繰り返し指定可（例: `sort=buzzScore,desc&sort=postedAt,desc`） |
| `q` | string | なし | キーワード検索文字列（検索系エンドポイントで使用） |
| `platform` | string | なし | プラットフォームコード（`INSTAGRAM`/`TIKTOK`/`X`/`YOUTUBE`/`PINTEREST`/`THREADS`）でのフィルタ |
| `postType` | string | なし | 投稿種別（`REEL`/`IMAGE`/`VIDEO`/`CAROUSEL`/`TEXT`）でのフィルタ |
| `genre` | string | なし | ジャンルでのフィルタ |
| `dateFrom` / `dateTo` | string(date) | なし | 投稿日時の範囲フィルタ（`YYYY-MM-DD`） |
| `minBuzzScore` / `maxBuzzScore` | number | なし | BuzzScoreの範囲フィルタ |

一覧レスポンスは共通のページングエンベロープで返却します。

```json
{
  "content": [ /* 要素配列 */ ],
  "page": 0,
  "size": 20,
  "totalElements": 134,
  "totalPages": 7,
  "sort": "buzzScore,desc"
}
```

## 3. 共通エラーレスポンス形式

すべてのエラーは以下の形式で返却します（RFC 7807 Problem Detailsを簡略化した独自形式）。

```json
{
  "timestamp": "2026-07-18T10:15:30Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "code": "INVALID_POST_URL",
  "message": "投稿URLの形式が不正、または対応していないプラットフォームです。",
  "path": "/api/v1/posts/analyze",
  "details": [
    { "field": "postUrl", "reason": "サポート対象外のURL形式です。" }
  ]
}
```

| HTTPステータス | error | 主な用途 |
|-----------------|-------|----------|
| 400 | BAD_REQUEST | リクエスト形式不正、バリデーションエラー |
| 401 | UNAUTHORIZED | 未認証・トークン無効 |
| 403 | FORBIDDEN | 権限不足（ロール/プラン制限） |
| 404 | NOT_FOUND | リソースが存在しない |
| 409 | CONFLICT | 一意制約違反（例: 既に登録済みの競合アカウント） |
| 422 | UNPROCESSABLE_ENTITY | 投稿が非公開・削除済みなど、意味的に処理不能 |
| 429 | TOO_MANY_REQUESTS | レート制限超過（自プラットフォームAPI or 外部SNS API起因） |
| 500 | INTERNAL_SERVER_ERROR | サーバー内部エラー |
| 502 | BAD_GATEWAY | 外部SNS API/OpenAI APIとの連携エラー |

## 4. エンドポイント一覧

### 4.1 認証（Auth）

| メソッド | パス | 概要 |
|---------|------|------|
| POST | `/auth/register` | 新規ユーザー登録 |
| POST | `/auth/login` | ログイン（JWT発行） |
| POST | `/auth/refresh` | アクセストークン再発行 |
| POST | `/auth/logout` | ログアウト（リフレッシュトークン失効） |
| GET | `/auth/me` | ログイン中ユーザー情報取得 |

**POST `/auth/login` リクエスト例**
```json
{ "email": "user@example.com", "password": "P@ssw0rd123" }
```

**POST `/auth/login` レスポンス例（200 OK）**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": { "id": "6f1a...", "email": "user@example.com", "name": "山田太郎", "role": "USER", "plan": "FREE" }
}
```

### 4.2 プラットフォーム（Platforms）

| メソッド | パス | 概要 |
|---------|------|------|
| GET | `/platforms` | 対応プラットフォーム一覧取得（フロントのフィルタUI用） |

### 4.3 投稿分析（Posts / Analysis）

| メソッド | パス | 概要 |
|---------|------|------|
| POST | `/posts/analyze` | 投稿URLを分析（データ取得→AI分析→BuzzScore算出→改善案生成→類似投稿提案） |
| GET | `/posts` | 投稿検索（キーワード/ハッシュタグ/アカウント/プラットフォーム等でフィルタ） |
| GET | `/posts/{postId}` | 投稿詳細取得 |
| GET | `/posts/{postId}/analysis` | 投稿のAI分析結果取得 |
| GET | `/posts/{postId}/buzz-score` | 投稿のBuzzScore内訳取得 |
| GET | `/posts/{postId}/metrics` | 投稿のメトリクス時系列取得 |
| GET | `/posts/{postId}/similar` | 類似投稿一覧取得 |

**POST `/posts/analyze` リクエスト例**
```json
{ "postUrl": "https://www.instagram.com/reel/Cxxxxxxxxxx/" }
```

**POST `/posts/analyze` レスポンス例（201 Created）**
```json
{
  "post": {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "platform": "INSTAGRAM",
    "postUrl": "https://www.instagram.com/reel/Cxxxxxxxxxx/",
    "postType": "REEL",
    "caption": "今日から始める朝活ルーティン #朝活 #習慣化",
    "postedAt": "2026-07-10T09:00:00Z",
    "videoDurationSec": 32,
    "imageCount": null,
    "likeCount": 128500,
    "commentCount": 2140,
    "viewCount": 1850000,
    "shareCount": 9800,
    "hashtags": ["朝活", "習慣化"]
  },
  "analysisResult": {
    "id": "9d2c...",
    "status": "COMPLETED",
    "summary": "冒頭2秒で結果を提示するフックと、テンポの速いカット割りが視聴維持率を高めています。",
    "targetAudience": "20代〜30代のセルフケア・生産性向上に関心のある層",
    "hookAnalysis": "「朝活で人生が変わった理由」という結果先出し型フック",
    "ctaAnalysis": "保存を促す明示的なCTAあり（「保存して後で見返してね」）",
    "sentimentAnalysis": { "primaryEmotion": "共感", "score": 0.82 },
    "improvementSuggestions": [
      "動画冒頭1秒以内にテキストオーバーレイを追加する",
      "コメント欄への返信率を高め、初速のエンゲージメントを強化する"
    ],
    "similarPosts": ["a1b2...", "c3d4..."]
  },
  "buzzScore": {
    "totalScore": 84.5,
    "engagementScore": 88.0,
    "viewScore": 90.2,
    "commentRateScore": 75.4,
    "formatScore": 80.0,
    "hashtagScore": 70.0,
    "postingTimeScore": 82.0,
    "textStructureScore": 78.5,
    "aiInsightScore": 91.0,
    "strategyVersion": "v1"
  }
}
```

### 4.4 検索（Search）

| メソッド | パス | 概要 |
|---------|------|------|
| GET | `/search?type=keyword&q=...` | キーワード検索（投稿を横断検索） |
| GET | `/search?type=hashtag&q=...` | ハッシュタグ検索 |
| GET | `/search?type=account&q=...` | アカウント検索 |

### 4.5 SNSアカウント・競合分析（Social Accounts / Competitors）

| メソッド | パス | 概要 |
|---------|------|------|
| POST | `/social-accounts` | 分析対象アカウント（競合含む）を登録 |
| GET | `/social-accounts` | 登録済みアカウント一覧取得 |
| GET | `/social-accounts/{accountId}` | アカウント詳細取得 |
| DELETE | `/social-accounts/{accountId}` | アカウント追跡解除 |
| GET | `/social-accounts/{accountId}/posts` | アカウントの投稿一覧取得 |
| GET | `/competitors/{accountId}/analysis` | 競合統計取得（`period=WEEKLY\|MONTHLY\|ALL_TIME`） |
| GET | `/competitors/{accountId}/ranking` | アカウント内の伸びる投稿ランキング取得 |
| GET | `/competitors/compare?accountIds=...` | 複数アカウントの統計比較取得 |

**GET `/competitors/{accountId}/analysis?period=WEEKLY` レスポンス例（200 OK）**
```json
{
  "socialAccount": { "id": "acc-001", "platform": "TIKTOK", "username": "example_creator", "followerCount": 452000 },
  "periodType": "WEEKLY",
  "periodStart": "2026-07-06",
  "periodEnd": "2026-07-12",
  "postCount": 5,
  "avgLikeCount": 34200.5,
  "avgCommentCount": 812.4,
  "avgViewCount": 610000.0,
  "postingFrequencyPerWeek": 5.0,
  "avgVideoDurationSec": 28.6,
  "avgCaptionLength": 64.2,
  "dominantPostType": "VIDEO",
  "dominantGenre": "ライフスタイル",
  "topPostingHour": 20
}
```

### 4.6 トレンド（Trends）

| メソッド | パス | 概要 |
|---------|------|------|
| GET | `/trends` | プラットフォーム/ジャンル横断のトレンド取得（`platform`、`genre`、`period`でフィルタ） |

### 4.7 ランキング（Rankings）

| メソッド | パス | 概要 |
|---------|------|------|
| GET | `/rankings` | ランキング取得（`type=TRENDING\|WEEKLY\|MONTHLY\|GENRE\|PLATFORM`、`platform`、`genre`でフィルタ） |

**GET `/rankings?type=WEEKLY&platform=INSTAGRAM&page=0&size=10` レスポンス例（200 OK）**
```json
{
  "content": [
    {
      "rankPosition": 1,
      "score": 96.2,
      "post": {
        "id": "post-101",
        "platform": "INSTAGRAM",
        "postType": "REEL",
        "postUrl": "https://www.instagram.com/reel/Cyyyyyyyyyy/",
        "likeCount": 240000,
        "commentCount": 5400
      }
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10,
  "sort": "rankPosition,asc"
}
```

### 4.7-A 定期データ取得バッチ（Sync / 自動化）

| メソッド | パス | 概要 |
|---------|------|------|
| POST | `/sync/run` | 追跡対象アカウントの最新公開投稿を各SNS公式APIから再取得し、BuzzScore再計算・ランキング再構築を即時実行する（手動トリガー）。 |

同等の処理は `batch.sync.enabled=true` の場合、`batch.sync.cron`（デフォルト毎時0分）に従いサーバー側で自動実行される。AI分析（OpenAI呼び出し）はコスト抑制のためこのバッチでは行わず、公開メトリクスに基づくBuzzScore再計算のみを行う。

**POST `/sync/run` レスポンス例（200 OK）**
```json
{
  "trackedAccountCount": 12,
  "syncedPostCount": 87,
  "updatedRankingCount": 180,
  "accountErrors": [],
  "startedAt": "2026-07-18T05:00:00Z",
  "finishedAt": "2026-07-18T05:00:04Z"
}
```

### 4.8 AIレポート（Reports）

| メソッド | パス | 概要 |
|---------|------|------|
| POST | `/reports` | レポート生成（`analysisResultId`、`format`を指定） |
| GET | `/reports` | 生成済みレポート一覧取得 |
| GET | `/reports/{reportId}` | レポート詳細取得 |
| GET | `/reports/{reportId}/download` | レポートファイルダウンロード（302リダイレクト or バイナリ） |

**POST `/reports` リクエスト例**
```json
{ "analysisResultId": "9d2c...", "format": "PDF", "title": "朝活ルーティン投稿 分析レポート" }
```

### 4.9 保存済み分析（Saved Analyses）

| メソッド | パス | 概要 |
|---------|------|------|
| POST | `/saved-analyses` | 分析結果を保存 |
| GET | `/saved-analyses` | 保存済み分析一覧取得（`folderName`でフィルタ） |
| PATCH | `/saved-analyses/{id}` | メモ・フォルダ名の更新 |
| DELETE | `/saved-analyses/{id}` | 保存済み分析の削除 |

### 4.10 設定（Settings）

| メソッド | パス | 概要 |
|---------|------|------|
| GET | `/settings` | ユーザー設定取得 |
| PUT | `/settings` | ユーザー設定更新（通知/デフォルトSNS/言語/タイムゾーン） |

## 5. 非同期処理を伴うエンドポイントの扱い

`POST /posts/analyze` および `POST /reports` は、内部でAI分析・外部API呼び出しなど時間のかかる処理を伴います。処理時間の見積りに応じて以下いずれかの応答パターンを取ります。

- **同期応答**：数秒以内に完了見込みの場合、`201 Created` で結果本体を返す（上記レスポンス例の通り）。
- **非同期応答**：処理が長時間化する場合、`202 Accepted` を返し、`analysisResult.status = "PROCESSING"` とジョブ確認用の `GET /posts/{postId}/analysis` を案内する。フロントエンドはポーリングまたはWebSocket通知（将来拡張）で完了を検知する。

## 6. レート制限（本システム自身のAPI）

| プラン | リクエスト上限 |
|--------|----------------|
| FREE | 60 req/分、投稿URL分析 10件/日 |
| PRO | 300 req/分、投稿URL分析 200件/日 |
| ENTERPRISE | 個別契約 |

上限超過時は `429 TOO_MANY_REQUESTS` を返し、レスポンスヘッダー `Retry-After` に再試行可能までの秒数を付与します。
