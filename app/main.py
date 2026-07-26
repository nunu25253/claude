"""FastAPIアプリのエントリポイント。

`create_app()` をファクトリ関数にしているのは、テスト側で毎回新しいアプリ
インスタンス(独立した app.state)を作れるようにするため。モジュール直下に
`app = FastAPI()` を置くとテスト間で状態(キャッシュ・DB接続・CostGuardの
1日カウンタ等)が漏れてしまう。
"""

from pathlib import Path

from fastapi import FastAPI
from fastapi.responses import FileResponse
from fastapi.staticfiles import StaticFiles

from app.api import routes_designs, routes_favorites, routes_stats
from app.api.errors import register_exception_handlers
from app.config import Settings
from app.core.cache import LRUTTLCache
from app.core.cost_guard import BudgetPolicy, CostGuard
from app.models.db import create_session_factory
from app.services.design_service import GenerationResult

_STATIC_DIR = Path(__file__).parent / "static"


def create_app(settings: Settings | None = None) -> FastAPI:
    """NailMuseのFastAPIアプリケーションを構築する。

    `settings`を引数で受け取れるようにしているのは、テストで予算上限や
    DATABASE_URL(インメモリDBにする等)を差し替えられるようにするため。
    共有インスタンス(DBセッションファクトリ・CostGuard・キャッシュ)は
    グローバル変数ではなく`app.state`に載せ、DI経由で各ルートに渡す(§4)。
    """
    app = FastAPI(title="NailMuse")
    app.state.settings = settings or Settings()

    app.state.session_factory = create_session_factory(app.state.settings.database_url)
    app.state.cost_guard = CostGuard(
        BudgetPolicy(
            max_calls_per_day=app.state.settings.budget_max_calls_per_day,
            max_cost_usd_per_day=app.state.settings.budget_max_cost_usd_per_day,
        )
    )
    app.state.cache = LRUTTLCache[GenerationResult](
        max_size=app.state.settings.cache_max_size,
        ttl_seconds=app.state.settings.cache_ttl_seconds,
    )

    @app.get("/health")
    def health() -> dict[str, str]:
        """ヘルスチェック用エンドポイント。"""
        return {"status": "ok"}

    app.include_router(routes_designs.router)
    app.include_router(routes_designs.image_router)
    app.include_router(routes_favorites.router)
    app.include_router(routes_stats.router)

    register_exception_handlers(app)

    # フロントエンド(vanilla JS + Tailwind CDN)はビルド工程なしで静的配信する。
    app.mount("/static", StaticFiles(directory=_STATIC_DIR), name="static")

    @app.get("/")
    def index() -> FileResponse:
        """1ページ構成のフロントエンドを返す。"""
        return FileResponse(_STATIC_DIR / "index.html")

    return app
