"""DIの集約点(§11)。

設計理由: FastAPIの`Depends`を使うと、テスト側で本物のProviderをスパイや
fakeへ差し替えられる。CostGuardやキャッシュは`app.state`に1つだけ置き、
リクエストのたびにnewしないことで「1日の集計」という状態を維持する
(§4のグローバル可変状態禁止に対応するため、モジュールグローバル変数ではなく
app.stateに保持している)。
"""

from collections.abc import Iterator

from fastapi import Depends, Request
from sqlalchemy.orm import Session

from app.config import Settings
from app.core.cache import LRUTTLCache
from app.core.cost_guard import CostGuard
from app.providers.base import AIProvider
from app.providers.mock_provider import MockAIProvider
from app.services.design_service import DesignService, GenerationResult


def get_settings(request: Request) -> Settings:
    """app.stateに保持しているSettingsを返す。"""
    settings: Settings = request.app.state.settings
    return settings


def get_db(request: Request) -> Iterator[Session]:
    """リクエスト単位のSessionを作り、後始末(close)まで面倒を見る。"""
    session_factory = request.app.state.session_factory
    session: Session = session_factory()
    try:
        yield session
    finally:
        session.close()


def get_provider(settings: Settings = Depends(get_settings)) -> AIProvider:
    """AIプロバイダを返す。

    NOTE: AI_PROVIDER環境変数によるreal/mock切り替えはPhase 5で追加する
    (RealAIProviderは現時点でスタブすら存在しないため)。それまではmock固定。
    """
    return MockAIProvider(latency_ms=settings.mock_latency_ms)


def get_cost_guard(request: Request) -> CostGuard:
    """app.stateで使い回しているCostGuardを返す(1日ごとの集計を維持するため)。"""
    cost_guard: CostGuard = request.app.state.cost_guard
    return cost_guard


def get_cache(request: Request) -> LRUTTLCache[GenerationResult]:
    """app.stateで使い回しているキャッシュを返す。"""
    cache: LRUTTLCache[GenerationResult] = request.app.state.cache
    return cache


def get_design_service(
    provider: AIProvider = Depends(get_provider),
    cost_guard: CostGuard = Depends(get_cost_guard),
    cache: LRUTTLCache[GenerationResult] = Depends(get_cache),
) -> DesignService:
    """provider/cost_guard/cacheを束ねたDesignServiceを組み立てて返す。"""
    return DesignService(provider=provider, cost_guard=cost_guard, cache=cache)
