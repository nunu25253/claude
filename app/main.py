"""FastAPIアプリのエントリポイント。

`create_app()` をファクトリ関数にしているのは、テスト側で毎回新しいアプリ
インスタンス(独立した app.state)を作れるようにするため。モジュール直下に
`app = FastAPI()` を置くとテスト間で状態(キャッシュ・DB接続等)が漏れてしまう。
"""

from fastapi import FastAPI


def create_app() -> FastAPI:
    """NailMuseのFastAPIアプリケーションを構築する。"""
    app = FastAPI(title="NailMuse")

    @app.get("/health")
    def health() -> dict[str, str]:
        """ヘルスチェック用エンドポイント。"""
        return {"status": "ok"}

    # NOTE: 静的配信・APIルーター登録・例外ハンドラ登録は Phase 3/4 で追加する。
    return app
