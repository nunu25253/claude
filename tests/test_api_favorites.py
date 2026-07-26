"""`/api/favorites` のAPIテスト(§8、TestClient)。"""

from fastapi.testclient import TestClient

VALID_PAYLOAD = {
    "scene": "office",
    "colors": ["beige"],
    "shape": "oval",
    "length": "short",
    "style": "simple",
}


def _create_proposal_id(client: TestClient, colors: list[str] | None = None) -> str:
    payload = {**VALID_PAYLOAD, "colors": colors} if colors else VALID_PAYLOAD
    response = client.post("/api/designs/generate", json=payload)
    proposal_id: str = response.json()["proposals"][0]["id"]
    return proposal_id


def test_add_favorite_is_idempotent(client: TestClient) -> None:
    """初回登録は201、同じproposal_idの2回目は200で冪等になる。"""
    proposal_id = _create_proposal_id(client)

    first = client.post("/api/favorites", json={"proposal_id": proposal_id})
    second = client.post("/api/favorites", json={"proposal_id": proposal_id})

    assert first.status_code == 201
    assert second.status_code == 200


def test_add_favorite_for_missing_proposal_returns_404(client: TestClient) -> None:
    """存在しないproposal_idへのお気に入り登録は404 not_foundになる。"""
    response = client.post("/api/favorites", json={"proposal_id": "does-not-exist"})

    assert response.status_code == 404
    assert response.json()["error"]["code"] == "not_found"


def test_delete_favorite(client: TestClient) -> None:
    """登録済みのお気に入りは204で削除でき、未登録への削除は404になる。"""
    proposal_id = _create_proposal_id(client)
    client.post("/api/favorites", json={"proposal_id": proposal_id})

    delete_response = client.delete(f"/api/favorites/{proposal_id}")
    missing_response = client.delete(f"/api/favorites/{proposal_id}")

    assert delete_response.status_code == 204
    assert missing_response.status_code == 404


def test_list_favorites_newest_first(client: TestClient) -> None:
    """お気に入り一覧は新しい順で返る。"""
    first_id = _create_proposal_id(client, colors=["red"])
    second_id = _create_proposal_id(client, colors=["blue"])
    client.post("/api/favorites", json={"proposal_id": first_id})
    client.post("/api/favorites", json={"proposal_id": second_id})

    response = client.get("/api/favorites")

    assert response.status_code == 200
    ids = [item["id"] for item in response.json()]
    assert ids == [second_id, first_id]
