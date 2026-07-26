"""CostGuard / BudgetPolicyのテスト(§6.3)。"""

from datetime import date

import pytest

from app.core.cost_guard import BudgetExceededError, BudgetPolicy, CostGuard
from app.models.domain import Color, DesignProposal, DesignRequest, Length, Scene, Shape, Style


class _FakeProvider:
    """コストとポリシーの境界だけを確認したいので、生成内容は最小限にするダブル。"""

    def __init__(self, cost: float) -> None:
        self.estimated_cost_usd_per_call = cost
        self.calls = 0

    def generate_designs(self, request: DesignRequest, seed: int) -> list[DesignProposal]:
        self.calls += 1
        return []


def _sample_request() -> DesignRequest:
    return DesignRequest(
        scene=Scene.office,
        colors=[Color.beige],
        shape=Shape.oval,
        length=Length.short,
        style=Style.simple,
    )


def test_counts_every_call() -> None:
    """呼び出しのたびに回数とコストが積み上がる。"""
    guard = CostGuard(BudgetPolicy(max_calls_per_day=10, max_cost_usd_per_day=1.0))
    provider = _FakeProvider(cost=0.02)

    guard.call(provider, _sample_request(), seed=1)
    guard.call(provider, _sample_request(), seed=2)

    assert guard.calls_today == 2
    assert guard.estimated_cost_today_usd == pytest.approx(0.04)
    assert provider.calls == 2


def test_raises_over_call_limit() -> None:
    """回数の上限を超えるとBudgetExceededErrorになり、Providerは呼ばれない。"""
    guard = CostGuard(BudgetPolicy(max_calls_per_day=1, max_cost_usd_per_day=1.0))
    provider = _FakeProvider(cost=0.0)

    guard.call(provider, _sample_request(), seed=1)
    with pytest.raises(BudgetExceededError):
        guard.call(provider, _sample_request(), seed=2)

    assert provider.calls == 1


def test_raises_over_cost_limit() -> None:
    """コストの上限を超えるとBudgetExceededErrorになる。"""
    guard = CostGuard(BudgetPolicy(max_calls_per_day=100, max_cost_usd_per_day=0.01))
    provider = _FakeProvider(cost=0.02)

    with pytest.raises(BudgetExceededError):
        guard.call(provider, _sample_request(), seed=1)

    assert provider.calls == 0


def test_resets_on_new_day() -> None:
    """日付が変わるとカウンタが自動的にリセットされる(fake today_fnで再現)。"""
    current_day = [date(2026, 7, 26)]
    guard = CostGuard(
        BudgetPolicy(max_calls_per_day=1, max_cost_usd_per_day=1.0),
        today_fn=lambda: current_day[0],
    )
    provider = _FakeProvider(cost=0.0)

    guard.call(provider, _sample_request(), seed=1)
    with pytest.raises(BudgetExceededError):
        guard.call(provider, _sample_request(), seed=2)

    current_day[0] = date(2026, 7, 27)
    guard.call(provider, _sample_request(), seed=3)  # 新しい日なので上限超過にならない

    assert guard.calls_today == 1
    assert provider.calls == 2
