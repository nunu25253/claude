# NailMuse

AIネイルデザイン提案アプリの動くプロトタイプ。実AI APIには一切接続せず、
決定的なモック生成(`MockAIProvider`)だけで「条件を選ぶ→3案生成→♡保存→
お気に入り/履歴で閲覧」という一連の体験を再現する。

## 前提条件

- Python 3.12
- [uv](https://docs.astral.sh/uv/)(推奨。無い場合は`venv + pip`でも可)
- インターネット接続(フロントエンドがTailwind CSSをCDNから読み込むため。
  バックエンドAPI自体は`AI_PROVIDER=mock`の間、外部通信を一切行わない)

## セットアップ

```bash
uv sync
```

`uv`が無い場合は`venv + pip`にフォールバックできる:

```bash
python3.12 -m venv .venv
source .venv/bin/activate
pip install -e .
pip install pytest pytest-cov httpx ruff mypy
```

必要なら`.env.example`を`.env`にコピーして値を調整する(コピーしなくても
§14の既定値がそのまま使われるので、プロトタイプとして動かすだけなら不要):

```bash
cp .env.example .env
```

## 起動

```bash
uv run uvicorn app.main:create_app --factory --reload
```

- `http://127.0.0.1:8000/` : フロントエンド(条件入力→生成→お気に入り→履歴)
- `http://127.0.0.1:8000/health` : ヘルスチェック(`{"status": "ok"}`)
- `http://127.0.0.1:8000/docs` : FastAPIが自動生成するAPIドキュメント(Swagger UI)

初回起動時に`data/nailmuse.db`(SQLite)が自動作成される。

## テスト・品質チェック

```bash
uv run ruff check .
uv run mypy .
uv run pytest -q
```

カバレッジ込みで実行する場合:

```bash
uv run pytest --cov=app --cov-report=term-missing -q
```

## 設定一覧(`.env.example` / `app/config.py`)

| 環境変数 | 既定値 | 説明 |
|---|---|---|
| `AI_PROVIDER` | `mock` | `mock`(実AI費用ゼロ)か`real`(未実装スタブ、呼ぶとエラー)か |
| `MOCK_LATENCY_MS` | `0` | Mock生成の擬似レイテンシ(ms)。0で無効。テストでは必ず0にする |
| `CACHE_MAX_SIZE` | `256` | 生成結果キャッシュの最大件数(LRUで追い出す) |
| `CACHE_TTL_SECONDS` | `3600` | キャッシュの有効期限(秒) |
| `BUDGET_MAX_CALLS_PER_DAY` | `200` | 1日あたりのProvider呼び出し上限回数 |
| `BUDGET_MAX_COST_USD_PER_DAY` | `1.00` | 1日あたりの推定コスト上限(USD) |
| `DATABASE_URL` | `sqlite:///data/nailmuse.db` | SQLAlchemyの接続文字列 |

`AI_PROVIDER=mock`の間は、上記のどの設定を変えても外部AI APIへの通信は
発生しない(`MockAIProvider`はテンプレートと乱数だけで生成する)。

## 構成図

```
ブラウザ
  │  (Tailwind CDN + vanilla JS、ビルド工程なし)
  ▼
FastAPI (app/main.py: create_app)
  ├─ /                      静的配信(app/static/index.html, app.js)
  ├─ /health                ヘルスチェック
  └─ /api
      ├─ POST /designs/generate      ─┐
      ├─ GET  /designs/history        │  app/api/routes_designs.py
      ├─ GET  /proposals/{id}/image.svg (SVGはimage_service.pyが生成)
      ├─ POST/DELETE/GET /favorites   … app/api/routes_favorites.py
      └─ GET  /stats                  … app/api/routes_stats.py
              │
              ▼
      app/api/deps.py (DI集約点)
              │
              ▼
      app/services/design_service.py
        sanitize_free_text (app/core/security.py: プロンプトインジェクション対策)
          → build_cache_key (§6.4のキー生成手順)
          → app/core/cache.py (LRU+TTLキャッシュ) ヒットならここで返す
          → app/core/cost_guard.py (CostGuard: 1日の呼び出し/コスト上限)
              → app/providers/mock_provider.py (決定的なテンプレート生成)
                 ※ app/providers/real_provider.py はスタブのみ(呼ぶとNotImplementedError)
          → app/models/db.py (generations/proposals/favoritesをSQLiteへ保存)
```

## スコープ外(構造上の受け口のみ)

実AI接続・画像生成AI・AR試着・ネイル健康診断・ユーザー認証・決済は実装していない。
詳細は`PROGRESS.md`の「将来メモ」を参照。
