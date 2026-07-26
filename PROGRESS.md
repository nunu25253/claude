# PROGRESS

## 現在地
- 完了済み: Phase 0
- 次: Phase 1

## 将来メモ(スコープ外の提案置き場)
- (まだなし)

## ログ

### 2026-07-26 Phase 0 完了
- 完了: pyproject.toml(ruff/mypy設定含む)、全ディレクトリと空モジュール、`GET /health`、README雛形、.gitignore、PROGRESS.md雛形を作成。
- 未解決: なし。
- 判断と理由:
  1. config.py はインフラ的な設定値の宣言のみで業務ロジックを含まないため、Phase 0のうちに§14の環境変数に対応するSettingsクラスとして完成させた(先回り実装の禁止は、Phase固有のアルゴリズム・振る舞いを対象とする判断)。
  2. 他のモジュール(cache/cost_guard/security/providers/services/models/api配下)は1行docstringのみの空モジュールとし、各Phaseで実装する。
  3. pytest 9系は「収集テスト0件」を終了コード5(失敗扱い)にするため、「テスト0件でも通ること」の字面通りにするより、Phase 0で唯一実在する機能(`/health`)に対する最小スモークテスト(tests/conftest.py + tests/test_health.py)を追加する方が本来の意図(品質ゲートが機能すること)に合致すると判断した。
