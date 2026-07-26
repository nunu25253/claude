"""統計情報(`/api/stats`)のAPIルーター(§8)。"""

from fastapi import APIRouter, Depends
from pydantic import BaseModel

from app.api.deps import get_cache, get_cost_guard
from app.core.cache import LRUTTLCache
from app.core.cost_guard import CostGuard
from app.services.design_service import GenerationResult

router = APIRouter(prefix="/api", tags=["stats"])


class CacheStats(BaseModel):
    """キャッシュの利用状況。"""

    size: int
    hit: int
    miss: int
    hit_rate: float


class StatsResponse(BaseModel):
    """`GET /api/stats`のレスポンス。"""

    calls_today: int
    estimated_cost_today_usd: float
    cache: CacheStats


@router.get("/stats", response_model=StatsResponse)
def get_stats(
    cost_guard: CostGuard = Depends(get_cost_guard),
    cache: LRUTTLCache[GenerationResult] = Depends(get_cache),
) -> StatsResponse:
    """本日の呼び出し状況とキャッシュ状況をまとめて返す(§8)。"""
    return StatsResponse(
        calls_today=cost_guard.calls_today,
        estimated_cost_today_usd=cost_guard.estimated_cost_today_usd,
        cache=CacheStats(
            size=cache.size,
            hit=cache.hit_count,
            miss=cache.miss_count,
            hit_rate=cache.hit_rate,
        ),
    )
