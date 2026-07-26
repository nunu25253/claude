# NailMuse

AIネイルデザイン提案アプリの動くプロトタイプ(実AI API費用ゼロ)。

> 本READMEはPhase 0時点の雛形です。前提条件・設定一覧・構成図はPhase 5で完成させます。

## セットアップ

```bash
uv sync
```

`uv` が無い場合:

```bash
python3.12 -m venv .venv
source .venv/bin/activate
pip install -e ".[dev]"
```

## 起動

```bash
uv run uvicorn app.main:create_app --factory --reload
```

起動後 `http://127.0.0.1:8000/health` にアクセスし `{"status": "ok"}` が返ることを確認する。

## テスト・品質チェック

```bash
uv run ruff check .
uv run mypy .
uv run pytest -q
```
