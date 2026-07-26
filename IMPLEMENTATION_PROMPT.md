# NailMuse プロトタイプ実装プロンプト v2(Claude Code用・詳細版)

> **使い方**
> 1. 空のリポジトリ直下にこのファイルを `IMPLEMENTATION_PROMPT.md` として置く
> 2. Claude Code に送る: 「`IMPLEMENTATION_PROMPT.md` を読み、ルールに従って **Phase 0 から**開始。各Phase完了時は必ず停止して報告してください」
> 3. 各Phaseの報告(§13のフォーマット)を確認 → 「承認。次のPhaseへ」で進める
> 4. 中断後の再開は「`PROGRESS.md` を読んで続きから再開」でよい

---

## 1. あなたの役割

あなたは Python / FastAPI に精通したシニアエンジニアです。目的は、AIネイルデザインアプリ **NailMuse** の「動くプロトタイプ」を、**実AI APIの費用ゼロ**で実装することです。

このコードは発注者(初心者〜中級者)の教材も兼ねます。重要な設計判断にはコメントで「**なぜそうするのか**」を書き、専門用語には(かっこ)で一言補足を添えてください。

コメントの期待水準の例:

```python
# DI(依存性注入: 部品を外から渡す設計。テスト時に本物をモックへ差し替えられる)で
# Providerを受け取る。内部でnewすると差し替えられずテスト不能になるため。
```

## 2. プロダクト仕様

### 2.1 ユーザーフロー
1. 条件を選ぶ → 2. 「デザインを生成」→ 3. AIが3案をカード表示 → 4. 気に入った案を♡保存 → 5. お気に入り / 履歴タブでいつでも閲覧

### 2.2 入力項目(すべて必須。自由記述のみ任意)

| 項目 | 内部値(enum) | 画面表示 | 形式 |
|---|---|---|---|
| シーン `scene` | office / date / bridal / event / daily | オフィス / デート / ブライダル / イベント / 普段使い | 単一選択 |
| 色 `colors` | red / pink / beige / white / black / blue / green / purple / gold / silver | 各色スウォッチ | 1〜3個の複数選択 |
| 爪の形 `shape` | round / oval / square / almond | ラウンド / オーバル / スクエア / アーモンド | 単一選択 |
| 長さ `length` | short / medium / long | ショート / ミディアム / ロング | 単一選択 |
| スタイル `style` | simple / french / gradient / nuance / one_color | シンプル / フレンチ / グラデーション / ニュアンス / ワンカラー | 単一選択 |
| 自由記述 `free_text` | — | 「その他の要望(例: 大人っぽく、ラメ少なめ)」 | 任意 / 最大120文字 |

### 2.3 出力(1回の生成 = 3案)

各案(`DesignProposal`)は次を持つ:
- `title`: 15文字前後のデザイン名(例: 「オフィスの静けさベージュ」)
- `concept`: 80〜150文字のコンセプト説明
- `palette`: HEXカラー3〜5色(1色目がベースカラー)
- `points`: 施術・セルフネイルのポイント 2〜4項目(各40文字以内)
- `image_url`: モック画像(サーバ生成SVG。§9)

### 2.4 スコープ外(実装しない。構造上の受け口だけ用意)
実AI接続 / 画像生成AI / AR試着 / ネイル健康診断 / ユーザー認証 / 決済。提案したい場合はPROGRESS.mdの「将来メモ」に書くだけにする。

## 3. 技術スタック(固定・変更禁止)

- Python 3.12 / FastAPI / Uvicorn(エンドポイントは `def` の同期でよい。理由: プロトタイプ規模では非同期の複雑さが利益を上回るため。コメントで明記)
- SQLite + SQLAlchemy 2.x(ORM。生SQL禁止。将来PostgreSQLへ移行するため)
- Pydantic v2 + pydantic-settings(設定管理)
- フロント: FastAPI配信の静的 `index.html` + Tailwind CSS(CDN)+ vanilla JS(ビルド工程なし)
- パッケージ管理: `uv`(なければ `venv + pip` にフォールバック)
- 品質: `pytest` / `pytest-cov` / `ruff` / `mypy` / APIテストは `httpx` の TestClient

## 4. コーディング規約

- 全関数に型ヒント。公開関数にはdocstring(日本語1行要約。設計上の理由があれば「設計理由: …」を追記)
- コメントは「何をしているか」ではなく「なぜそうするか」を書く
- ユーザー向けメッセージは日本語、ログは英語でも日本語でも可
- グローバル可変状態は禁止。共有インスタンス(キャッシュ等)は `app.state` + FastAPIのDI経由で渡す
- 命名: ファイル/関数は snake_case、クラスは PascalCase、定数は UPPER_SNAKE
- マジックナンバー禁止。設定値は `config.py`(§14の環境変数)へ

## 5. データモデル(契約として固定)

`app/models/domain.py` に定義。フィールド名・型は変更禁止(フロント/テストとの契約のため)。

```python
class Scene(str, Enum):
    office = "office"; date = "date"; bridal = "bridal"; event = "event"; daily = "daily"

class Shape(str, Enum):
    round = "round"; oval = "oval"; square = "square"; almond = "almond"

class Length(str, Enum):
    short = "short"; medium = "medium"; long = "long"

class Style(str, Enum):
    simple = "simple"; french = "french"; gradient = "gradient"
    nuance = "nuance"; one_color = "one_color"

class Color(str, Enum):
    red = "red"; pink = "pink"; beige = "beige"; white = "white"; black = "black"
    blue = "blue"; green = "green"; purple = "purple"; gold = "gold"; silver = "silver"

class DesignRequest(BaseModel):
    scene: Scene
    colors: list[Color] = Field(min_length=1, max_length=3)
    shape: Shape
    length: Length
    style: Style
    free_text: str | None = Field(default=None, max_length=120)

class DesignProposal(BaseModel):
    id: str                      # uuid4
    title: str
    concept: str
    palette: list[str]           # "#RRGGBB" 3〜5色
    points: list[str]            # 2〜4項目
    image_url: str               # "/api/proposals/{id}/image.svg"

class GenerateResponse(BaseModel):
    generation_id: str
    cache_hit: bool
    proposals: list[DesignProposal]   # 常に3件
```

## 6. アーキテクチャ必須要件(詳細・省略禁止)

### 6.1 AIProvider 抽象化(`app/providers/`)

```python
class AIProvider(Protocol):
    def generate_designs(self, request: DesignRequest, seed: int) -> list[DesignProposal]: ...
```

- 選択は環境変数 `AI_PROVIDER`(mock / real)。デフォルトは必ず mock
- `RealAIProvider` はスタブのみ: 呼ばれたら `NotImplementedError("Phase外。実接続時に実装")`。将来書く場所を `# TODO:` で示す
- FastAPIの `Depends` で注入し、テストで差し替え可能にする

### 6.2 MockAIProvider の生成アルゴリズム(決定的であること)

1. `random.Random(seed)` を使う(`seed` はキャッシュキー先頭16桁を16進整数化した値。同じ入力→同じ3案。理由: キャッシュ検証と再現性)
2. テンプレート表を持つ(各10件程度用意。以下は例):
   - `TITLE_PARTS[style]` 例: simple → ["静けさ", "すっきり", "ミニマル"] / french → ["きちんと", "上品ライン", "クラシック"]
   - `SCENE_WORDS[scene]` 例: office → "オフィス" / bridal → "ブライダル"
   - `CONCEPT_TEMPLATES[scene]` 例: office → "手元を上品に見せる◯◯配色。会議でも浮かない落ち着きと、ふとした瞬間の華やかさを両立します。"
   - `POINT_POOL[style]` 例: gradient → ["根元は薄く、爪先へ濃く重ねる", "スポンジでぼかすとムラが出にくい"]
3. `palette` は `COLOR_BASE_HEX`(例: pink → "#F4B6C2", beige → "#E8D3C0" …全10色定義)から選択色を並べ、明度を±10%変化させた派生色を1〜2色追加して3〜5色にする
4. 3案は「1案目=王道 / 2案目=遊びを1つ足す / 3案目=引き算」の方針でテンプレート選択をずらす
5. `free_text` は無害化済み文字列(§6.5)を concept 末尾に「ご要望の◯◯も意識しました」と1文で反映(存在する場合のみ)
6. `MOCK_LATENCY_MS` > 0 なら sleep して実API風の待ち時間を再現(デフォルト0。テストでは必ず0)

### 6.3 CostGuard / BudgetPolicy(`app/core/cost_guard.py`)

- すべてのProvider呼び出しは `CostGuard.call(provider, request, seed)` 経由。直接呼び出し禁止
- `BudgetPolicy(max_calls_per_day: int, max_cost_usd_per_day: float)`。コスト単価は provider 側の `estimated_cost_usd_per_call` 属性(mock=0.0 / real=0.02)から取得
- 日付が変わったらカウンタ自動リセット。現在日付は `today_fn: Callable[[], date]` として注入(理由: テストで日付跨ぎを再現するため)
- 超過時は `BudgetExceededError` → API層で **429** に変換
- メモリ保持のためプロセス再起動でリセットされる。プロトタイプでは許容し、その旨コメントに書く

### 6.4 キャッシュ(`app/core/cache.py`)

- キー生成手順(この順で固定):
  1. `free_text` を NFKC正規化 → strip → 小文字化 → 連続空白を1つに
  2. `colors` をソート
  3. `{"scene":…,"colors":…,"shape":…,"length":…,"style":…,"free_text":…}` を `json.dumps(…, sort_keys=True, ensure_ascii=False, separators=(",", ":"))`
  4. その SHA256 hexdigest をキーにする(例: `"a3f9…"` 64桁)
- 実装: LRU + TTL。`max_size` 超過で最古参を追い出し、`ttl_seconds` 経過分はヒット扱いしない
- 現在時刻は `now_fn: Callable[[], float]` を注入(理由: sleepなしでTTL切れをテストするため)
- ヒット時は CostGuard も Provider も呼ばない。hit / miss を記録し `/api/stats` で公開

### 6.5 プロンプトインジェクション対策(`app/core/security.py`)

Mock段階でも実装する(実接続時に後付けすると漏れるため)。

- `sanitize_free_text(raw: str) -> str`: 制御文字(Unicodeカテゴリ Cc)除去 → NFKC → strip → 120文字超は 422
- 拒否パターン(大文字小文字無視・部分一致。`SUSPICIOUS_PATTERNS` として定数化):
  - `ignore (all|previous|above)` / `以前の指示` / `指示を無視` / `無視して`
  - `system prompt` / `システムプロンプト` / `<system>` / バッククォート3連(コードフェンス)
  - `you are now` / `あなたは今から` / `assistant:`
  - URL(`http://` `https://`)
- 検知時は 422(code: `suspicious_input`)+ `logger.warning` に先頭50文字とSHA256を記録
- 将来のプロンプト構築用に `build_prompt(request) -> str` を services に用意し、ユーザー入力は必ず `<user_input>…</user_input>` 内へ「データとして」埋め込む。指示文セクションへのf-string混入は禁止(このルール自体をdocstringに書く)

## 7. データベーススキーマ(SQLAlchemy・SQLite)

```
generations   id TEXT PK / request_json TEXT / cache_key TEXT(index) / created_at DATETIME
proposals     id TEXT PK / generation_id FK→generations.id / title TEXT / concept TEXT
              palette_json TEXT / points_json TEXT / sort_order INTEGER(0〜2)
favorites     id INTEGER PK AUTOINC / proposal_id TEXT FK→proposals.id UNIQUE / created_at DATETIME
```

- 生成のたびに generations + proposals×3 を保存(キャッシュヒット時は保存済みを再利用し、新規保存しない)
- JSONカラムは `json.dumps(ensure_ascii=False)` で保存

## 8. API仕様

エラーは全エンドポイント共通フォーマット:

```json
{"error": {"code": "budget_exceeded", "message": "本日の生成上限に達しました。明日また試してください。"}}
```

codes: `validation_error`(422) / `suspicious_input`(422) / `budget_exceeded`(429) / `not_found`(404) / `internal`(500)

### POST /api/designs/generate
リクエスト:
```json
{"scene": "office", "colors": ["beige", "pink"], "shape": "oval",
 "length": "short", "style": "simple", "free_text": "大人っぽく"}
```
レスポンス 200:
```json
{"generation_id": "1f3c…", "cache_hit": false,
 "proposals": [{"id": "9d2a…", "title": "オフィスの静けさベージュ",
   "concept": "手元を上品に…", "palette": ["#E8D3C0", "#F4B6C2", "#FFFFFF"],
   "points": ["ベースは薄づき2度塗り", "トップは艶感重視"],
   "image_url": "/api/proposals/9d2a…/image.svg"}, "…あと2件"]}
```

### その他
- `GET /api/proposals/{id}/image.svg` → `image/svg+xml`(§9)。存在しないidは404
- `GET /api/designs/history?limit=20&offset=0` → 新しい順。limit最大50。proposals入れ子で返す
- `POST /api/favorites` body `{"proposal_id": "…"}` → 201(既に登録済みなら200。冪等)
- `DELETE /api/favorites/{proposal_id}` → 204(なければ404)
- `GET /api/favorites` → お気に入りproposalの配列(新しい順)
- `GET /api/stats` → `{"calls_today": 12, "estimated_cost_today_usd": 0.0, "cache": {"size": 34, "hit": 80, "miss": 40, "hit_rate": 0.667}}`
- `GET /health` → `{"status": "ok"}`

## 9. モック画像(SVG)仕様(`app/services/image_service.py`)

- viewBox `0 0 400 240`、背景 `#FAF7F5`、爪5本を中央に横並び(幅48・間隔16)
- `shape` で輪郭を変える: round=角丸大 / square=角丸小 / oval=楕円 / almond=先端を尖らせた楕円(近似でよい)
- `style` で塗りを変える:
  - one_color / simple → palette[0] 単色(simpleは白ハイライト小を追加)
  - french → ベースをbeige系、先端20%を palette[0] の帯
  - gradient → `linearGradient` で palette[0]→[1](→[2])を縦方向に
  - nuance → palette から2〜3色の半透明円(opacity 0.6〜0.8)を重ねる
- 5本のうち1〜2本のスタイルを軽く変えると「デザイン感」が出る(seed由来の乱数で決定的に)
- `Cache-Control: max-age=86400` を付与

## 10. フロントエンド仕様(1ページ・スマホ幅優先)

- ヘッダー「NailMuse」+ タブ3つ: **作成 / お気に入り / 履歴**(vanilla JSでタブ切替)
- 作成タブ: §2.2の項目を上から順に。単一選択=チップ(選択中は塗り)、色=丸スウォッチ(複数選択・選択中はリング)、自由記述=文字数カウンタ付き(「12/120」)。最下部に固定CTA「デザインを生成」
- 生成中はスケルトンカード3枚。完了で3案カード(SVG画像 → タイトル → コンセプト → パレットのスウォッチ列 → ポイント箇条書き → ♡ボタン)
- ♡はトグル。お気に入りタブは同じカードUIで一覧、履歴タブは生成日時ごとのグループ表示(タップで3案を展開)
- 429/422はトースト表示(例: 429→「本日の上限に達しました」/ suspicious_input→「入力内容を確認してください」)
- Tailwindはユーティリティクラスのみ。`max-w-md mx-auto` で中央寄せ。JSはfetch + 素朴なrender関数で十分(フレームワーク導入禁止)

## 11. ディレクトリ構成(ファイル単位で固定)

```
nailmuse/
├── IMPLEMENTATION_PROMPT.md
├── PROGRESS.md
├── README.md
├── pyproject.toml
├── .env.example
├── app/
│   ├── __init__.py
│   ├── main.py                  # create_app() ファクトリ + 静的配信 + 例外ハンドラ登録
│   ├── config.py                # pydantic-settings の Settings
│   ├── core/
│   │   ├── cache.py             # LRU+TTLキャッシュ
│   │   ├── cost_guard.py        # CostGuard / BudgetPolicy / BudgetExceededError
│   │   └── security.py          # sanitize_free_text / SUSPICIOUS_PATTERNS
│   ├── providers/
│   │   ├── base.py              # AIProvider Protocol
│   │   ├── mock_provider.py     # テンプレート表 + 決定的生成
│   │   └── real_provider.py     # スタブ(NotImplementedError)
│   ├── services/
│   │   ├── design_service.py    # 正規化→キャッシュ→CostGuard→Provider→保存
│   │   └── image_service.py     # SVG生成
│   ├── models/
│   │   ├── domain.py            # §5のPydanticモデル
│   │   └── db.py                # SQLAlchemyモデル + セッション
│   ├── api/
│   │   ├── deps.py              # get_settings / get_db / get_service(DIの集約点)
│   │   ├── errors.py            # 共通エラーレスポンス変換
│   │   ├── routes_designs.py
│   │   ├── routes_favorites.py
│   │   └── routes_stats.py
│   └── static/
│       ├── index.html
│       └── app.js
└── tests/
    ├── conftest.py              # テスト用Settings上書き・TestClient・fake clock
    ├── test_cache.py
    ├── test_cost_guard.py
    ├── test_mock_provider.py
    ├── test_security.py
    ├── test_design_service.py
    ├── test_api_designs.py
    ├── test_api_favorites.py
    └── test_image_service.py
```

(各パッケージの `__init__.py` は省略表記。実際には作成する)

## 12. 開発フェーズ(1つずつ。承認なしで次へ進むの禁止)

### Phase 0: セットアップ
**成果物**: pyproject.toml(依存: fastapi / uvicorn[standard] / pydantic / pydantic-settings / sqlalchemy、dev: pytest / pytest-cov / httpx / ruff / mypy)、ruff設定(line-length=100, select=["E","F","I","B","UP"])、mypy設定(python_version=3.12, disallow_untyped_defs=true)、全ディレクトリと空モジュール、`GET /health`、README雛形(起動・テストコマンド)、PROGRESS.md雛形(§13)
**チェックリスト**: `uvicorn app.main:create_app --factory` 起動 → `/health` が200 / `ruff check .`・`mypy .`・`pytest` すべて成功(テスト0件でも通ること)

### Phase 1: ドメイン + コア部品
**成果物**: §5のdomain.py / base.py / mock_provider.py / cache.py / cost_guard.py
**必須テスト**:
- test_mock_provider: `test_same_seed_same_output`(全フィールド一致)/ `test_returns_three_proposals` / `test_palette_is_valid_hex`(`#RRGGBB`正規表現)/ `test_free_text_reflected_in_concept`
- test_cache: `test_miss_then_hit` / `test_lru_evicts_oldest` / `test_ttl_expiry`(fake clockで時間を進める。sleep禁止)
- test_cost_guard: `test_counts_every_call` / `test_raises_over_call_limit` / `test_raises_over_cost_limit` / `test_resets_on_new_day`(fake today_fn)
**チェックリスト**: APIなしでコア動作がテストのみで実証されている / カバレッジ core+providers 90%以上

### Phase 2: デザイン生成サービス
**成果物**: security.py / design_service.py(パイプライン: sanitize → 正規化キー生成 → キャッシュ照会 → CostGuard経由でProvider → 結果保存はPhase 3まで保留しインメモリ)
**必須テスト**:
- test_security: `test_rejects_each_suspicious_pattern`(§6.5の全パターンをparametrize)/ `test_strips_control_chars` / `test_over_120_chars_rejected`
- test_design_service: `test_cache_key_stable_under_spacing_and_case`(「ピンク 多め」と「 ぴんく  多め 」が同一キー…NFKC後の比較)/ `test_provider_called_once_for_same_input`(スパイProviderで呼び出し回数=1)/ `test_budget_exceeded_propagates`
**チェックリスト**: 悪意入力の拒否と、同一入力でProviderが1回しか呼ばれないことがテストで証明されている

### Phase 3: API + 永続化
**成果物**: db.py(§7)/ deps.py / errors.py / routes三種 / design_serviceの保存処理
**必須テスト**(TestClient): 生成の正常系(3件・cache_hit=false→2回目true)/ 不正enumで422 / suspicious_inputで422 / 上限超過で429(テスト用にmax_calls=2へ上書き)/ favoritesのPOST冪等・DELETE 204・404 / historyのlimit/offset / statsの形式
**チェックリスト**: 429・422・404のエラーパスがすべてテスト済み / DBファイルは `data/nailmuse.db`(gitignore)

### Phase 4: フロントエンド
**成果物**: index.html / app.js / image_service.py + SVGルート(§9)
**必須テスト**: test_image_service(`svg`タグを含む / palette色が出力に含まれる / shapeごとに出力が変わる / 同一proposalで決定的)
**手動チェックリスト**(報告に結果を明記): スマホ幅で崩れない / 入力→生成→3案表示 / ♡トグルがお気に入りタブへ反映 / 履歴に積まれる / 429時トースト表示(上限2に下げて確認)/ 文字数カウンタ動作
**チェックリスト**: ブラウザで通しのユーザーフローが動く

### Phase 5: 仕上げ
**成果物**: real_provider.pyスタブ + AI_PROVIDER切替 / .env.example(§14)/ README完成(前提条件→セットアップ→起動→テスト→設定一覧→構成図(テキストでよい))
**チェックリスト**: `pytest --cov=app --cov-report=term-missing` で core / services / providers 80%以上 / `AI_PROVIDER=mock` で外部通信ゼロ / READMEだけで第三者が再現可能 / §16をすべて満たす

## 13. 進行ルール・報告フォーマット

- 各Phase終了時に必ず実行して結果をそのまま貼る: `ruff check .` / `mypy .` / `pytest -q`(Phase 5は `--cov` 付き)
- **Phase完了報告の固定フォーマット**:
  1. 作成・変更ファイル一覧
  2. コマンド実行結果(貼り付け)
  3. 設計上の判断と理由(3〜5点)
  4. 手を抜いた箇所・既知のリスク
  5. 次Phaseの予定
  → ここで**停止**し、承認を待つ
- `PROGRESS.md` は毎Phase追記。雛形:

```markdown
# PROGRESS
## 現在地
- 完了済み: Phase 0, 1
- 次: Phase 2
## 将来メモ(スコープ外の提案置き場)
- …
## ログ
### 2026-07-27 Phase 1 完了
- 完了: …
- 未解決: …
- 判断と理由: …
```

- 仕様の矛盾・不明点に気づいたら、実装せず選択肢を挙げて質問する
- このファイル(IMPLEMENTATION_PROMPT.md)自体は編集禁止

## 14. .env.example(この内容で作成)

```env
# AIプロバイダ: mock | real(デフォルトmock。realはPhase外)
AI_PROVIDER=mock
# Mockの擬似レイテンシ(ms)。0で無効。テストでは必ず0
MOCK_LATENCY_MS=0
# キャッシュ
CACHE_MAX_SIZE=256
CACHE_TTL_SECONDS=3600
# コスト防御
BUDGET_MAX_CALLS_PER_DAY=200
BUDGET_MAX_COST_USD_PER_DAY=1.00
# DB
DATABASE_URL=sqlite:///data/nailmuse.db
# 実AI接続時に追加(今は使わない): OPENAI_API_KEY / ANTHROPIC_API_KEY 等
```

## 15. 禁止事項と判断基準

**禁止**: 実APIの呼び出し / APIキー・秘密情報のコード直書き / 生SQL / フロントへのフレームワーク導入(React等)/ Phaseの先回り実装 / テストを消して緑にする / 承認前の次Phase着手 / §5モデルと§8レスポンス形式の変更

**迷ったときの判断基準**(上から優先):
1. このファイルの記述 > 一般的なベストプラクティス
2. シンプルさ > 拡張性(ただし§6の4部品はDIを守る)
3. 決定的・テスト可能 > リアルさ
4. それでも決められない場合は質問する(選択肢+推奨案つきで)

## 16. 完了の定義

- `uvicorn` 起動後、ブラウザで「条件入力 → 3案表示 → ♡保存 → お気に入り/履歴閲覧」が通しで動く
- `ruff` / `mypy` / `pytest` 全成功、core / services / providers のカバレッジ80%以上
- README の手順だけで第三者がセットアップ〜起動〜テストを再現できる
- `AI_PROVIDER=mock` のまま外部通信が一切発生しない(CostGuard・キャッシュ・インジェクション対策が全経路で有効)
