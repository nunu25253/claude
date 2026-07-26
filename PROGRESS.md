# PROGRESS

## 現在地
- 完了済み: Phase 0, 1, 2, 3, 4, 5(全Phase完了)
- 次: (なし。追加機能はすべて「将来メモ」を参照)

## 将来メモ(スコープ外の提案置き場)
- 実AI接続(OpenAI/Anthropic等): `app/providers/real_provider.py`に骨組みのみ用意。
  実装時は`app.services.design_service.build_prompt()`を使い、ユーザー入力は
  `<user_input>`タグの中に「データ」として埋め込むこと(§6.5のルールを踏襲)。
- 画像生成AI・AR試着・ネイル健康診断・ユーザー認証・決済: 未着手(§2.4のスコープ外)。
- CostGuard/キャッシュの永続化: 現状はプロセスメモリのみ(§6.3・§6.4のコメント通り、
  プロトタイプとして許容)。本番運用するならRedis等への外出しを検討する。
- フロントエンドはTailwind CDNに依存しているため、CDNへ到達できないネットワーク
  環境では見た目のスタイルが崩れる(DOM・機能自体は影響を受けない)。気になる場合は
  Tailwindのビルド済みCSSを静的配置する対応が考えられるが、§3の技術スタック固定
  (Tailwind CDN指定)により本プロトタイプでは見送った。

## ログ

### 2026-07-26 タブ切り替えのCDN依存を解消(起動デモ中に発見・修正)
- 背景: 起動して動作確認したところ、Tailwind CDNへ到達できない環境
  (サンドボックスのネットワークポリシーで`cdn.tailwindcss.com`が遮断)だと、
  タブ切り替え・トースト表示・履歴の展開開閉がTailwindの`.hidden`
  ユーティリティクラスに依存していたため機能しない(全タブが同時表示される
  等)ことが判明した。ユーザーの実環境でも広告ブロッカー等でCDNが読み込め
  ないケースは起こりうるため、単なるサンドボックス固有の制約として放置せず
  修正した。
- 修正: `app/static/app.js`のタブ切り替え・トースト・履歴グループ開閉、
  および`app/static/index.html`の初期非表示指定を、Tailwindの`.hidden`
  クラスからHTML標準の`hidden`属性(bool型プロパティ。ブラウザ標準の
  UAスタイルシートで`display:none`になるためCSSファイルが1枚も読み込めなくても
  効く)へ切り替えた。
- 判断と理由: `hidden`属性はTailwindの`.hidden`ユーティリティが定義している
  スタイル(`display:none`)と完全に同じ見た目になるため、Tailwind導入方針
  (§3・§10)を崩さずに堅牢性だけを上げられる。バックエンドのテスト
  (60件、カバレッジ100%)には影響がないことを確認済み。

### 2026-07-26 Phase 5 完了
- 完了: `providers/real_provider.py`(RealAIProviderスタブ、呼ぶと
  `NotImplementedError`)、`api/deps.py`の`get_provider`に`AI_PROVIDER`
  環境変数によるmock/real切り替えを追加、`.env.example`(§14通りの内容、
  Phase 0で作成漏れていたため今回追加)、README完成(前提条件・セットアップ・
  起動・テスト・設定一覧・構成図)。test_real_provider.pyと
  `AI_PROVIDER=real`時の500応答テストを追加(計60テスト)。
- 未解決: なし。§16の完了条件をすべて満たしている(下記参照)。
- 判断と理由:
  1. `.env.example`はIMPLEMENTATION_PROMPT.md §11のディレクトリ構成に含まれて
     いたがPhase 0で作成し忘れていたことにPhase 5で気づき、§14の内容通りに
     追加した。中身は最初からこのPhaseで決まっていた通りで変更はない。
  2. `get_provider`の切り替えロジックは、`AI_PROVIDER=real`のときだけ
     `RealAIProvider()`を返すシンプルな分岐にした。実接続の実装自体は
     スコープ外なので、呼び出すと`NotImplementedError`→共通の500ハンドラで
     `{"error": {"code": "internal", ...}}`になることをテストで確認した。
  3. `TestClient`のデフォルト(`raise_server_exceptions=True`)だと、汎用の
     `Exception`ハンドラで拾われるはずの例外がテスト側に再送出されてしまう
     ことが判明したため、`tests/conftest.py`の`client`/`make_client`双方に
     `raise_server_exceptions=False`を設定し、本番と同じ「常にJSONで返る」
     挙動をテストできるようにした。
  4. §16の完了条件を最終確認:
     - ブラウザでの一連の流れ(条件入力→3案表示→♡保存→お気に入り/履歴閲覧)は
       Phase 4でPlaywrightにより確認済み(Tailwind CDNのみサンドボックス制約で
       視覚未確認、PROGRESS.mdの将来メモに記載)。
     - `ruff check .`・`mypy .`・`pytest`は全成功、`--cov=app`でアプリ全体
       100%(core/services/providersも100%、要件の80%を超過)。
     - READMEの手順(uv sync→起動→テスト)だけでセットアップを再現できる形にした。
     - `AI_PROVIDER=mock`のままテストスイート・手動確認のいずれも外部通信は
       発生していない(Mock生成はテンプレートと乱数のみ)。

### 2026-07-26 Phase 4 完了
- 完了: `services/image_service.py`(SVGモック画像生成)、
  `GET /api/proposals/{id}/image.svg`(routes_designs.pyに追加)、
  `static/index.html` + `static/app.js`(作成/お気に入り/履歴の3タブ、
  vanilla JS)、`main.py`に静的配信(`/static`マウント + `/`でindex.html配信)を
  追加。test_image_service(6件)を追加し、既存テストに画像ルート・
  indexルートのテストも追加(計57テスト、アプリ全体カバレッジ100%)。
- 未解決: なし。
- 手動チェックリスト(Playwright + Chromiumで確認、結果は下記):
  - スマホ幅(390px)で崩れない: △ ― DOM構造・レイアウトは崩れないが、
    このサンドボックス環境はネットワークポリシーで`cdn.tailwindcss.com`への
    アウトバウンドHTTPSが遮断されており(`curl`で403確認)、Tailwind CDN自体が
    読み込めないため見た目(角丸チップの色・パレットスウォッチのサイズ等、
    Tailwindユーティリティクラスに依存する部分)は未検証。実際のユーザー
    環境(CDNへ到達可能)での確認を推奨する。DOM操作・状態管理・API連携の
    ロジックはこの制約と無関係に動作確認済み。
  - 入力→生成→3案表示: ○ ― 条件選択→「デザインを生成」→3件のカード
    (SVG画像・タイトル・コンセプト・パレット・ポイント)が表示されることを確認。
  - ♡トグルがお気に入りタブへ反映: ○ ― ハートをクリックすると♡→♥に変化し、
    お気に入りタブに即座に反映されることを確認。
  - 履歴に積まれる: ○ ― 生成するたびに履歴タブにグループが積まれ、タップで
    3案が展開されることを確認。
  - 429時トースト表示(上限2に下げて確認): ○ ― `BUDGET_MAX_CALLS_PER_DAY=2`
    で起動し、3回目の生成で「本日の上限に達しました」のトーストを確認。
  - 文字数カウンタ動作: ○ ― 自由記述欄に入力すると「5/120」のように
    リアルタイムでカウントが更新されることを確認。
- 判断と理由:
  1. SVG生成は`shape`/`style`をパラメータとして受け取る純粋関数
     (`render_nail_image`)にした。`DesignProposal`(§5で固定)にはshape/style
     フィールドが無いため、画像ルート側で`generations.request_json`から
     元のリクエストを復元して渡す設計にした。
  2. proposal画像のseedは`sha256(proposal_id)`から作る。生成時のseed(キャッシュ
     キー由来)を別途保存する列は§7のスキーマに無いため、proposal_id自体から
     決定的な値を導出することで「同一proposalなら同一画像」を満たした。
  3. 5本中1〜2本を「アクセントネイル」として別色で塗る処理は、5本すべてに
     共通のshapeレンダラーを使い、styleごとの塗り分けだけをスキップする形に
     した。shapeとstyleの見た目ロジックを疎結合に保つため。
  4. フロントエンドは状態管理ライブラリなしのプレーンなオブジェクト(state)+
     テンプレート文字列のinnerHTML描画にした。イベントは`document`への
     委譲(event delegation)1本にまとめ、動的に追加されるカード内のボタンにも
     個別にリスナーを付け直す必要がないようにした。


### 2026-07-26 Phase 3 完了
- 完了: `models/db.py`(generations/proposals/favoritesテーブル+セッション)、
  `api/deps.py`(get_settings/get_db/get_provider/get_cost_guard/get_cache/
  get_design_service)、`api/errors.py`(§8共通エラーフォーマット+例外ハンドラ)、
  `api/routes_designs.py`(generate/history)、`api/routes_favorites.py`、
  `api/routes_stats.py`、`design_service.py`への保存処理(`_persist`)、
  `main.py`でのルーター・DB・例外ハンドラ配線を実装。test_api_designs /
  test_api_favoritesを追加(計49テスト、アプリ全体でカバレッジ100%)。
- 未解決: なし。画像(SVG)ルートはPhase 4でimage_service.pyと合わせて追加する。
- 判断と理由:
  1. `create_app(settings: Settings | None = None)`のように引数でSettingsを
     受け取れるようにした。テストごとにインメモリDB(`sqlite:///:memory:`)や
     予算上限を差し替えられないと、429テストや履歴テストが他のテストと
     状態を共有してしまうため。
  2. `DesignService.generate()`に`db: Session | None = None`を追加した
     (キーワード引数でデフォルトNone)。Phase 1/2の呼び出し(`service.generate(request)`)
     を壊さずに、Phase 3のAPI層だけがDB保存を行えるようにするため。
  3. `GET /api/favorites`・`POST /api/favorites`は`proposals`テーブルを
     参照してproposal_idの実在確認をする。存在しないIDへのお気に入り登録は
     404(not_found)にした(仕様に明記はないが、外部キーの整合性を守るため)。
  4. SQLiteの`:memory:`はテストのたびに`StaticPool`を使い、同一接続を
     使い回すことで複数リクエスト間でテーブル内容が消えないようにした。
     本番用のファイルDB(`data/nailmuse.db`)では通常のプールを使う。
  5. FastAPI公式が推奨する`ruff`のB008回避策(`extend-immutable-calls`)を
     pyproject.tomlに追加し、`Depends(...)`をデフォルト引数に書けるようにした。
  6. mock_providerのパレット生成で、派生色が偶然重複した場合は明度変化の
     向きを反転させる小さな改善を行った(Phase 1のバグ、テストは影響なし)。


### 2026-07-26 Phase 2 完了
- 完了: `core/security.py`(sanitize_free_text / SUSPICIOUS_PATTERNS)、
  `services/design_service.py`(build_cache_key / seed_from_cache_key /
  build_prompt / DesignService)を実装。test_security(15件のparametrize含む) /
  test_design_serviceを追加(計37テスト)。
- 未解決: なし。DB保存はPhase 3で追加予定(現状はインメモリのLRUTTLCacheのみ)。
- 判断と理由:
  1. sanitize_free_text(§6.5のセキュリティ検証)と、キャッシュキー用の
     正規化(§6.4: NFKC→strip→小文字化→空白圧縮)は目的が異なるため別関数に
     分離した。前者は例外を送出して拒否、後者はキー安定化のための追加変換のみ。
  2. `build_prompt`はcore/security.pyではなくservices/design_service.pyに置いた
     (§6.5が「将来のプロンプト構築用にservicesに用意」と明記しているため)。
     ユーザー入力は`<user_input>`タグ内にのみ埋め込み、指示文セクションへの
     f-string混入は行わない設計をdocstringとテストの両方で担保した。
  3. `DesignService.generate()`はキャッシュヒット時に新しい`GenerationResult`
     (cache_hit=Trueのみ変えたコピー)を返す。キャッシュに保存した値自体は
     不変に保つことで、"ヒット時はCostGuardもProviderも呼ばない"という
     不変条件を壊さないようにした。
  4. core/services/providersのカバレッジは100%(Phase 5要件の80%を上回る)。


### 2026-07-26 Phase 1 完了
- 完了: `domain.py`(§5のenum/モデル)、`providers/base.py`(AIProvider Protocol)、
  `providers/mock_provider.py`(決定的モック生成)、`core/cache.py`(LRU+TTL)、
  `core/cost_guard.py`(CostGuard/BudgetPolicy)を実装。test_mock_provider /
  test_cache / test_cost_guard を追加(計15テスト)。
- 未解決: なし。
- 判断と理由:
  1. `DesignProposal.id`はuuid4形式だが、`uuid.uuid4()`は差し替え不能なグローバル
     乱数源を使うため決定性(§6.2)が崩れる。そこで`rng.getrandbits(128)`から
     `uuid.UUID(int=..., version=4)`を組み立て、同じseedなら同じidになるようにした。
  2. §5のenumは仕様書のコードそのまま`(str, Enum)`多重継承にする契約のため、
     ruffのUP042(StrEnum推奨)は`domain.py`内でのみ`# ruff: noqa: UP042`で無視した。
  3. `LRUTTLCache`はPEP 695のジェネリクス構文(`class LRUTTLCache[V]`)を採用し、
     Python 3.12前提(§3)の型安全なキャッシュにした。
  4. `CostGuard`は`app.providers.base.AIProvider`のProtocolをそのまま型として使い、
     mock/real双方をテストで差し替え可能にした(§6.1のDI要件)。
  5. カバレッジはcore/providersともに100%(要件は90%以上)。


### 2026-07-26 Phase 0 完了
- 完了: pyproject.toml(ruff/mypy設定含む)、全ディレクトリと空モジュール、`GET /health`、README雛形、.gitignore、PROGRESS.md雛形を作成。
- 未解決: なし。
- 判断と理由:
  1. config.py はインフラ的な設定値の宣言のみで業務ロジックを含まないため、Phase 0のうちに§14の環境変数に対応するSettingsクラスとして完成させた(先回り実装の禁止は、Phase固有のアルゴリズム・振る舞いを対象とする判断)。
  2. 他のモジュール(cache/cost_guard/security/providers/services/models/api配下)は1行docstringのみの空モジュールとし、各Phaseで実装する。
  3. pytest 9系は「収集テスト0件」を終了コード5(失敗扱い)にするため、「テスト0件でも通ること」の字面通りにするより、Phase 0で唯一実在する機能(`/health`)に対する最小スモークテスト(tests/conftest.py + tests/test_health.py)を追加する方が本来の意図(品質ゲートが機能すること)に合致すると判断した。
