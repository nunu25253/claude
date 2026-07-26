"""モックネイル画像(app/services/image_service.py)のテスト(§9)。"""

from app.models.domain import Shape, Style
from app.services.image_service import render_nail_image

PALETTE = ["#E8D3C0", "#F4B6C2", "#FFFFFF"]


def test_svg_contains_svg_tag() -> None:
    """出力は<svg ...>要素を含む有効なSVG断片であること。"""
    svg = render_nail_image(palette=PALETTE, shape=Shape.oval, style=Style.simple, seed=1)

    assert svg.startswith("<svg")
    assert svg.endswith("</svg>")


def test_palette_colors_are_included_for_gradient_style() -> None:
    """gradientスタイルでは、パレットの各色がSVG中に literal に現れる。"""
    svg = render_nail_image(palette=PALETTE, shape=Shape.round, style=Style.gradient, seed=2)

    for color in PALETTE:
        assert color in svg


def test_shape_changes_output() -> None:
    """shapeが違えば、同じpalette/style/seedでも出力が変わる。

    style=one_colorはハイライト等の余計な要素を追加しないため、
    形状ごとの要素(ellipse/rect)の違いだけを純粋に比較できる。
    """
    oval_svg = render_nail_image(palette=PALETTE, shape=Shape.oval, style=Style.one_color, seed=3)
    square_svg = render_nail_image(
        palette=PALETTE, shape=Shape.square, style=Style.one_color, seed=3
    )

    assert oval_svg != square_svg
    assert "<ellipse" in oval_svg
    assert "<ellipse" not in square_svg


def test_french_style_renders_tip_band() -> None:
    """frenchスタイルは、ベースカラー(palette[0])の先端の帯を含む。"""
    svg = render_nail_image(palette=PALETTE, shape=Shape.square, style=Style.french, seed=4)

    assert PALETTE[0] in svg


def test_deterministic_for_same_proposal() -> None:
    """同一palette/shape/style/seedなら、常に同じSVGになる。"""
    first = render_nail_image(palette=PALETTE, shape=Shape.almond, style=Style.nuance, seed=42)
    second = render_nail_image(palette=PALETTE, shape=Shape.almond, style=Style.nuance, seed=42)

    assert first == second
