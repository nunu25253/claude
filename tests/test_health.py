"""ヘルスチェックエンドポイントのテスト。"""

from fastapi.testclient import TestClient


def test_health_returns_ok(client: TestClient) -> None:
    """`GET /health` が200と{"status": "ok"}を返すことを確認する。"""
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "ok"}
