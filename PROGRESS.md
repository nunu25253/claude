# PROGRESS

## 現在地
- 完了済み: Phase 0, 1, 2
- 次: Phase 3

## 将来メモ(スコープ外の提案置き場)
- (まだなし)

## ログ

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
