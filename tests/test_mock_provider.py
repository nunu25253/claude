"""MockAIProviderの決定的生成を検証するテスト(§6.2)。"""

import re

from app.models.domain import Color, DesignRequest, Length, Scene, Shape, Style
from app.providers.mock_provider import MockAIProvider

HEX_COLOR_RE = re.compile(r"^#[0-9A-Fa-f]{6}$")


def _sample_request(free_text: str | None = None) -> DesignRequest:
    return DesignRequest(
        scene=Scene.office,
        colors=[Color.beige, Color.pink],
        shape=Shape.oval,
        length=Length.short,
        style=Style.simple,
        free_text=free_text,
    )


def test_same_seed_same_output() -> None:
    """同じseed・同じ入力なら、全フィールドが完全一致する3案が得られる。"""
    provider = MockAIProvider()
    request = _sample_request()

    first = provider.generate_designs(request, seed=12345)
    second = provider.generate_designs(request, seed=12345)

    assert first == second


def test_returns_three_proposals() -> None:
    """生成結果は常に3件であること。"""
    provider = MockAIProvider()
    proposals = provider.generate_designs(_sample_request(), seed=1)

    assert len(proposals) == 3


def test_palette_is_valid_hex() -> None:
    """paletteは3〜5色の"#RRGGBB"形式であること。"""
    provider = MockAIProvider()
    proposals = provider.generate_designs(_sample_request(), seed=42)

    for proposal in proposals:
        assert 3 <= len(proposal.palette) <= 5
        for color in proposal.palette:
            assert HEX_COLOR_RE.match(color)


def test_latency_ms_sleeps_before_returning() -> None:
    """MOCK_LATENCY_MSが正の値のとき、指定時間分スリープしてから返る。"""
    provider = MockAIProvider(latency_ms=1)
    proposals = provider.generate_designs(_sample_request(), seed=1)

    assert len(proposals) == 3


def test_free_text_reflected_in_concept() -> None:
    """free_textを指定すると、全案のconceptに反映される。"""
    provider = MockAIProvider()
    proposals = provider.generate_designs(_sample_request(free_text="大人っぽく"), seed=7)

    for proposal in proposals:
        assert "大人っぽく" in proposal.concept
