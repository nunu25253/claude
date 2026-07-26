"""pytest共通フィクスチャ。

設計理由: TestClientの生成をここに集約することで、各テストファイルが
`create_app()` の呼び出し方法を意識しなくて済むようにする。Phase 3以降で
DB・設定の上書きやfake clockもここに追加していく。
"""

from collections.abc import Iterator

import pytest
from fastapi.testclient import TestClient

from app.main import create_app


@pytest.fixture
def client() -> Iterator[TestClient]:
    """アプリ全体を通したAPIテスト用のTestClientを返す。"""
    app = create_app()
    with TestClient(app) as test_client:
        yield test_client
