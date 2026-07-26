"""LRU+TTLキャッシュ(app/core/cache.py)のテスト(§6.4)。"""

import pytest

from app.core.cache import LRUTTLCache


def test_miss_then_hit() -> None:
    """未登録キーはミス、登録後の同キーはヒットになる。"""
    cache: LRUTTLCache[str] = LRUTTLCache(max_size=10, ttl_seconds=60)

    assert cache.get("a") is None
    cache.set("a", "value-a")
    assert cache.get("a") == "value-a"
    assert cache.hit_count == 1
    assert cache.miss_count == 1


def test_lru_evicts_oldest() -> None:
    """上限を超えたら、最も長くアクセスされていないキーが追い出される。"""
    cache: LRUTTLCache[str] = LRUTTLCache(max_size=2, ttl_seconds=60)

    cache.set("a", "1")
    cache.set("b", "2")
    cache.get("a")  # aに触れることでaを最新扱いにする
    cache.set("c", "3")  # 上限超過。触れられていないbが最古参として追い出される

    assert cache.get("a") == "1"
    assert cache.get("b") is None
    assert cache.get("c") == "3"


def test_set_overwrites_existing_key() -> None:
    """既存キーへのsetは値を上書きし、件数は増えない。"""
    cache: LRUTTLCache[str] = LRUTTLCache(max_size=10, ttl_seconds=60)

    cache.set("a", "1")
    cache.set("a", "2")

    assert cache.get("a") == "2"
    assert cache.size == 1


def test_size_and_hit_rate() -> None:
    """size・hit_rateが呼び出し状況を正しく反映する。"""
    cache: LRUTTLCache[str] = LRUTTLCache(max_size=10, ttl_seconds=60)

    assert cache.size == 0
    assert cache.hit_rate == 0.0

    cache.set("a", "1")
    cache.get("a")  # hit
    cache.get("b")  # miss

    assert cache.size == 1
    assert cache.hit_rate == pytest.approx(0.5)


def test_ttl_expiry() -> None:
    """TTL経過後はヒット扱いされない(sleepを使わずfake clockで検証)。"""
    current_time = [0.0]
    cache: LRUTTLCache[str] = LRUTTLCache(
        max_size=10, ttl_seconds=100, now_fn=lambda: current_time[0]
    )

    cache.set("a", "1")
    current_time[0] = 50.0
    assert cache.get("a") == "1"  # まだTTL内

    current_time[0] = 200.0
    assert cache.get("a") is None  # TTL切れでミス扱い
