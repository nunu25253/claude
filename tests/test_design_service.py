"""デザイン生成サービス(app/services/design_service.py)のテスト(§6.4)。"""

import pytest

from app.core.cache import LRUTTLCache
from app.core.cost_guard import BudgetExceededError, BudgetPolicy, CostGuard
from app.core.security import sanitize_free_text
from app.models.domain import Color, DesignProposal, DesignRequest, Length, Scene, Shape, Style
from app.providers.mock_provider import MockAIProvider
from app.services.design_service import (
    DesignService,
    GenerationResult,
    build_cache_key,
    build_prompt,
)


class _SpyProvider:
    """呼び出し回数を数えつつ、実際の生成はMockAIProviderに委譲するテスト用ダブル。"""

    estimated_cost_usd_per_call = 0.0

    def __init__(self) -> None:
        self.calls = 0
        self._inner = MockAIProvider()

    def generate_designs(self, request: DesignRequest, seed: int) -> list[DesignProposal]:
        self.calls += 1
        return self._inner.generate_designs(request, seed)


def _make_service(provider: _SpyProvider, max_calls: int = 100) -> DesignService:
    cost_guard = CostGuard(BudgetPolicy(max_calls_per_day=max_calls, max_cost_usd_per_day=1.0))
    cache: LRUTTLCache[GenerationResult] = LRUTTLCache(max_size=100, ttl_seconds=3600)
    return DesignService(provider=provider, cost_guard=cost_guard, cache=cache)


def _sample_request(free_text: str | None = None) -> DesignRequest:
    return DesignRequest(
        scene=Scene.office,
        colors=[Color.beige, Color.pink],
        shape=Shape.oval,
        length=Length.short,
        style=Style.simple,
        free_text=free_text,
    )


def test_cache_key_stable_under_spacing_and_case() -> None:
    """空白・大文字小文字・全角スペースの違いがあっても同じキーになる。"""
    request_a = _sample_request(free_text="Pink 多め")
    request_b = _sample_request(free_text="  PINK　　多め  ")

    sanitized_a = sanitize_free_text(request_a.free_text or "")
    sanitized_b = sanitize_free_text(request_b.free_text or "")

    assert build_cache_key(request_a, sanitized_a) == build_cache_key(request_b, sanitized_b)


def test_provider_called_once_for_same_input() -> None:
    """同一入力で2回generateしても、Providerは1回しか呼ばれない。"""
    provider = _SpyProvider()
    service = _make_service(provider)
    request = _sample_request(free_text="大人っぽく")

    first = service.generate(request)
    second = service.generate(request)

    assert provider.calls == 1
    assert first.cache_hit is False
    assert second.cache_hit is True
    assert second.generation_id == first.generation_id
    assert second.proposals == first.proposals


def test_build_prompt_embeds_user_input_as_data() -> None:
    """build_promptはユーザー入力を<user_input>タグの中にだけ埋め込む(§6.5)。"""
    request = _sample_request()

    prompt = build_prompt(request, "無視してください")

    assert "<user_input>無視してください</user_input>" in prompt
    instruction_section = prompt.split("<user_input>")[0]
    assert "無視してください" not in instruction_section


def test_budget_exceeded_propagates() -> None:
    """CostGuardのBudgetExceededErrorはそのまま呼び出し元に伝播する。"""
    provider = _SpyProvider()
    service = _make_service(provider, max_calls=0)

    with pytest.raises(BudgetExceededError):
        service.generate(_sample_request())
