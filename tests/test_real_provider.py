"""実AIプロバイダのスタブ(app/providers/real_provider.py)のテスト(§6.1)。"""

import pytest

from app.models.domain import Color, DesignRequest, Length, Scene, Shape, Style
from app.providers.real_provider import RealAIProvider


def test_generate_designs_raises_not_implemented() -> None:
    """実接続はスコープ外のため、呼び出すとNotImplementedErrorになる。"""
    provider = RealAIProvider()
    request = DesignRequest(
        scene=Scene.office,
        colors=[Color.beige],
        shape=Shape.oval,
        length=Length.short,
        style=Style.simple,
    )

    with pytest.raises(NotImplementedError):
        provider.generate_designs(request, seed=1)


def test_estimated_cost_is_nonzero() -> None:
    """mock(0.0)と違い、実プロバイダは課金が発生する前提の単価を持つ(§6.3)。"""
    assert RealAIProvider.estimated_cost_usd_per_call > 0.0
