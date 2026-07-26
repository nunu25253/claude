"""モックネイル画像(SVG)生成(§9)。

設計理由: 実画像生成AIはスコープ外なので、条件(色・爪の形・スタイル)から
「それらしい」SVGをサーバ側で組み立てる。SVGはベクター画像でテキストとして
組み立てられるため、外部の画像生成ライブラリなしで済む(依存を増やさない)。
"""

import random

from app.models.domain import Shape, Style

_VIEW_WIDTH = 400
_VIEW_HEIGHT = 240
_BACKGROUND_COLOR = "#FAF7F5"

_NAIL_COUNT = 5
_NAIL_WIDTH = 48
_NAIL_SPACING = 16
_NAIL_HEIGHT = 160

_TOTAL_NAILS_WIDTH = _NAIL_COUNT * _NAIL_WIDTH + (_NAIL_COUNT - 1) * _NAIL_SPACING
_START_X = (_VIEW_WIDTH - _TOTAL_NAILS_WIDTH) / 2
_TOP_Y = (_VIEW_HEIGHT - _NAIL_HEIGHT) / 2

_FRENCH_BASE_HEX = "#F0E4D8"
_FRENCH_TIP_RATIO = 0.2
_NUANCE_BASE_HEX = "#F5F1EC"


def _round_nail(x: float, y: float, w: float, h: float, fill: str, opacity: float = 1.0) -> str:
    """丸みの大きい角丸(round=角丸大)。"""
    return (
        f'<rect x="{x:.1f}" y="{y:.1f}" width="{w}" height="{h}" rx="20" '
        f'fill="{fill}" opacity="{opacity}" />'
    )


def _square_nail(x: float, y: float, w: float, h: float, fill: str, opacity: float = 1.0) -> str:
    """丸みの小さい角丸(square=角丸小)。"""
    return (
        f'<rect x="{x:.1f}" y="{y:.1f}" width="{w}" height="{h}" rx="4" '
        f'fill="{fill}" opacity="{opacity}" />'
    )


def _oval_nail(x: float, y: float, w: float, h: float, fill: str, opacity: float = 1.0) -> str:
    """楕円(oval)。"""
    cx, cy = x + w / 2, y + h / 2
    return (
        f'<ellipse cx="{cx:.1f}" cy="{cy:.1f}" rx="{w / 2:.1f}" ry="{h / 2:.1f}" '
        f'fill="{fill}" opacity="{opacity}" />'
    )

def _almond_nail(x: float, y: float, w: float, h: float, fill: str, opacity: float = 1.0) -> str:
    """先端を尖らせた楕円の近似(almond)。上端を頂点にしたベジェ曲線で表現する。"""
    cx, right, bottom = x + w / 2, x + w, y + h
    upper_side_y, mid_y, waist_y = y + h * 0.15, y + h * 0.55, y + h * 0.7

    path = (
        f"M {cx:.1f} {y:.1f} "
        f"C {x + w * 0.9:.1f} {upper_side_y:.1f} {right:.1f} {mid_y:.1f} {right:.1f} {waist_y:.1f} "
        f"C {right:.1f} {bottom:.1f} {x:.1f} {bottom:.1f} {x:.1f} {waist_y:.1f} "
        f"C {x:.1f} {mid_y:.1f} {x + w * 0.1:.1f} {upper_side_y:.1f} {cx:.1f} {y:.1f} Z"
    )
    return f'<path d="{path}" fill="{fill}" opacity="{opacity}" />'


_SHAPE_RENDERERS = {
    Shape.round: _round_nail,
    Shape.square: _square_nail,
    Shape.oval: _oval_nail,
    Shape.almond: _almond_nail,
}


def _linear_gradient_defs(gradient_id: str, colors: list[str]) -> str:
    """縦方向のlinearGradientを定義する(§9のgradientスタイル用)。"""
    stop_count = len(colors)
    stops = "".join(
        f'<stop offset="{(index / max(stop_count - 1, 1)) * 100:.0f}%" stop-color="{color}" />'
        for index, color in enumerate(colors)
    )
    return (
        f'<linearGradient id="{gradient_id}" x1="0" y1="0" x2="0" y2="1">{stops}</linearGradient>'
    )


def _render_nail(
    shape: Shape, style: Style, palette: list[str], x: float, gradient_id: str
) -> tuple[str, str]:
    """1本分のSVG片を(defs用文字列, body用文字列)のペアで返す。"""
    renderer = _SHAPE_RENDERERS[shape]
    base_color = palette[0]

    if style in (Style.one_color, Style.simple):
        body = renderer(x, _TOP_Y, _NAIL_WIDTH, _NAIL_HEIGHT, base_color)
        if style is Style.simple:
            highlight_cx = x + _NAIL_WIDTH * 0.3
            highlight_cy = _TOP_Y + _NAIL_HEIGHT * 0.25
            body += (
                f'<ellipse cx="{highlight_cx:.1f}" cy="{highlight_cy:.1f}" '
                f'rx="{_NAIL_WIDTH * 0.12:.1f}" ry="{_NAIL_HEIGHT * 0.08:.1f}" '
                f'fill="#FFFFFF" opacity="0.5" />'
            )
        return "", body

    if style is Style.french:
        base = renderer(x, _TOP_Y, _NAIL_WIDTH, _NAIL_HEIGHT, _FRENCH_BASE_HEX)
        tip_height = _NAIL_HEIGHT * _FRENCH_TIP_RATIO
        tip = (
            f'<rect x="{x:.1f}" y="{_TOP_Y:.1f}" width="{_NAIL_WIDTH}" '
            f'height="{tip_height:.1f}" rx="10" fill="{base_color}" />'
        )
        return "", base + tip

    if style is Style.gradient:
        stops = palette[:3] if len(palette) >= 3 else palette[:2] or [base_color]
        gradient_defs = _linear_gradient_defs(gradient_id, stops)
        body = renderer(x, _TOP_Y, _NAIL_WIDTH, _NAIL_HEIGHT, f"url(#{gradient_id})")
        return gradient_defs, body

    # style is Style.nuance
    base = renderer(x, _TOP_Y, _NAIL_WIDTH, _NAIL_HEIGHT, _NUANCE_BASE_HEX)
    circles = "".join(
        f'<circle cx="{x + _NAIL_WIDTH * (0.3 + 0.2 * i):.1f}" '
        f'cy="{_TOP_Y + _NAIL_HEIGHT * (0.3 + 0.2 * i):.1f}" '
        f'r="{_NAIL_WIDTH * 0.35:.1f}" fill="{color}" opacity="{0.6 + 0.1 * (i % 2):.2f}" />'
        for i, color in enumerate(palette[:3])
    )
    return "", base + circles


def render_nail_image(palette: list[str], shape: Shape, style: Style, seed: int) -> str:
    """条件からモックのネイル画像(SVG文字列)を生成する(§9)。

    `seed`はproposal単位で固定の値を渡すこと(同一proposalなら同一SVGになる
    ようにするため)。5本のうち1〜2本だけ、パレットの別色をそのまま塗る
    「アクセントネイル」にすることで単調になりすぎないようにしている。
    """
    rng = random.Random(seed)
    accent_count = rng.choice((1, 2))
    accent_indices = set(rng.sample(range(_NAIL_COUNT), k=accent_count))

    defs_parts: list[str] = []
    body_parts: list[str] = []
    for index in range(_NAIL_COUNT):
        x = _START_X + index * (_NAIL_WIDTH + _NAIL_SPACING)

        if index in accent_indices and len(palette) > 1:
            accent_color = palette[(index + 1) % len(palette)]
            body_parts.append(
                _SHAPE_RENDERERS[shape](x, _TOP_Y, _NAIL_WIDTH, _NAIL_HEIGHT, accent_color)
            )
            continue

        defs_fragment, body_fragment = _render_nail(shape, style, palette, x, f"grad-{index}")
        if defs_fragment:
            defs_parts.append(defs_fragment)
        body_parts.append(body_fragment)

    defs = f"<defs>{''.join(defs_parts)}</defs>" if defs_parts else ""
    background = (
        f'<rect x="0" y="0" width="{_VIEW_WIDTH}" height="{_VIEW_HEIGHT}" '
        f'fill="{_BACKGROUND_COLOR}" />'
    )
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {_VIEW_WIDTH} {_VIEW_HEIGHT}">'
        f"{defs}{background}{''.join(body_parts)}</svg>"
    )
