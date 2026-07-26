"""ヘルスチェックエンドポイントのテスト。"""

from fastapi.testclient import TestClient


def test_health_returns_ok(client: TestClient) -> None:
    """`GET /health` が200と{"status": "ok"}を返すことを確認する。"""
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}


def test_index_serves_frontend(client: TestClient) -> None:
    """`GET /` がフロントエンド(index.html)をhtmlとして返す。"""
    response = client.get("/")

    assert response.status_code == 200
    assert "text/html" in response.headers["content-type"]
