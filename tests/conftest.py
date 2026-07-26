"""pytest共通フィクスチャ。

設計理由: TestClientの生成をここに集約することで、各テストファイルが
`create_app()` の呼び出し方法を意識しなくて済むようにする。DBは
インメモリSQLiteにし、予算上限は大きめにすることで、各テストが他の
テストの状態(DBの行やCostGuardのカウンタ)に影響されないようにする。
"""

from collections.abc import Callable, Iterator

import pytest
from fastapi.testclient import TestClient

from app.config import Settings
from app.main import create_app

ClientFactory = Callable[..., TestClient]


def _test_settings(**overrides: object) -> Settings:
    """テスト用の既定Settings(インメモリDB・十分な予算枠)。"""
    defaults: dict[str, object] = {
        "database_url": "sqlite:///:memory:",
        "budget_max_calls_per_day": 1000,
        "budget_max_cost_usd_per_day": 100.0,
    }
    defaults.update(overrides)
    return Settings(**defaults)  # type: ignore[arg-type]


@pytest.fixture
def client() -> Iterator[TestClient]:
    """既定設定(インメモリDB・十分な予算枠)でのTestClient。"""
    app = create_app(settings=_test_settings())
    with TestClient(app) as test_client:
        yield test_client


@pytest.fixture
def make_client() -> ClientFactory:
    """Settingsを上書きしたTestClientを作るためのファクトリ(429テスト等で使う)。"""

    def _factory(**overrides: object) -> TestClient:
        app = create_app(settings=_test_settings(**overrides))
        return TestClient(app)

    return _factory
