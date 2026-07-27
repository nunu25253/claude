"""モックネイル画像(SVG)生成(§9)。

設計理由: 実画像生成AIはスコープ外なので、条件(色・爪の形・スタイル)から
「それらしい」SVGをサーバ側で組み立てる。SVGはベクター画像でテキストとして
組み立てられるため、外部の画像生成ライブラリなしで済む(依存を増やさない)。
立体感(3D風)は、爪ごとに(1)ドロップシャドウ、(2)斜めのハイライト/シャドウ
グラデーション、(3)小さな光沢ハイライトを重ねることで表現する。いずれも
`clip-path`で爪の輪郭内に収め、はみ出さないようにしている。
"""

import random

from app.models.domain import Shape, Style

_VIEW_WIDTH = 400
_VIEW_HEIGHT = 240
_BACKGROUND_COLOR = "#FAF7F5"
_BACKGROUND_GRADIENT_ID = "bg-gradient"

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

# 全爪共通のドロップシャドウ・光沢ハイライト用ぼかしフィルタ(defsに1回だけ書く)。
_SHADOW_FILTER = (
    '<filter id="nail-shadow" x="-50%" y="-50%" width="200%" height="200%">'
    '<feDropShadow dx="0" dy="4" stdDeviation="4" flood-color="#000000" flood-opacity="0.25" />'
    "</filter>"
)
_HIGHLIGHT_BLUR_FILTER = '<filter id="soft-blur"><feGaussianBlur stdDeviation="1.2" /></filter>'


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


def _gloss_gradient_defs(gradient_id: str) -> str:
    """円柱状の立体感を出すための斜めグラデーション(左上ハイライト→右下シャドウ)。"""
    return (
        f'<linearGradient id="{gradient_id}" x1="0" y1="0" x2="1" y2="1">'
        '<stop offset="0%" stop-color="#FFFFFF" stop-opacity="0.55" />'
        '<stop offset="40%" stop-color="#FFFFFF" stop-opacity="0.08" />'
        '<stop offset="70%" stop-color="#000000" stop-opacity="0" />'
        '<stop offset="100%" stop-color="#000000" stop-opacity="0.22" />'
        "</linearGradient>"
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


def _background_markup() -> tuple[str, str]:
    """背景の放射グラデーション定義と、それを塗る矩形を返す(奥行きの演出)。"""
    gradient_defs = (
        f'<radialGradient id="{_BACKGROUND_GRADIENT_ID}" cx="50%" cy="35%" r="75%">'
        '<stop offset="0%" stop-color="#FFFFFF" />'
        f'<stop offset="100%" stop-color="{_BACKGROUND_COLOR}" />'
        "</radialGradient>"
    )
    rect = (
        f'<rect x="0" y="0" width="{_VIEW_WIDTH}" height="{_VIEW_HEIGHT}" '
        f'fill="url(#{_BACKGROUND_GRADIENT_ID})" />'
    )
    return gradient_defs, rect


def render_nail_image(palette: list[str], shape: Shape, style: Style, seed: int) -> str:
    """条件からモックのネイル画像(SVG文字列)を生成する(§9)。

    `seed`はproposal単位で固定の値を渡すこと(同一proposalなら同一画像になる
    ようにするため)。5本のうち1〜2本だけ、パレットの別色をそのまま塗る
    「アクセントネイル」にすることで単調になりすぎないようにしている。
    各爪には共通でドロップシャドウ・斜めのハイライト/シャドウグラデーション・
    小さな光沢ハイライトを重ね、立体感(3D風の質感)を出している。
    """
    rng = random.Random(seed)
    accent_count = rng.choice((1, 2))
    accent_indices = set(rng.sample(range(_NAIL_COUNT), k=accent_count))

    background_defs, background_rect = _background_markup()
    defs_parts: list[str] = [background_defs, _SHADOW_FILTER, _HIGHLIGHT_BLUR_FILTER]
    body_parts: list[str] = []

    for index in range(_NAIL_COUNT):
        x = _START_X + index * (_NAIL_WIDTH + _NAIL_SPACING)

        if index in accent_indices and len(palette) > 1:
            accent_color = palette[(index + 1) % len(palette)]
            base_fragment = _SHAPE_RENDERERS[shape](
                x, _TOP_Y, _NAIL_WIDTH, _NAIL_HEIGHT, accent_color
            )
        else:
            defs_fragment, base_fragment = _render_nail(shape, style, palette, x, f"grad-{index}")
            if defs_fragment:
                defs_parts.append(defs_fragment)

        # ドロップシャドウは<g>ごと適用する(内部が複数要素でも輪郭全体に1つの影になる)。
        body_parts.append(f'<g filter="url(#nail-shadow)">{base_fragment}</g>')

        # 立体感の演出(輪郭からはみ出さないようclip-pathで囲う)。
        clip_id = f"clip-{index}"
        gloss_id = f"gloss-{index}"
        defs_parts.append(
            f'<clipPath id="{clip_id}">'
            f'{_SHAPE_RENDERERS[shape](x, _TOP_Y, _NAIL_WIDTH, _NAIL_HEIGHT, "#000000")}'
            f"</clipPath>"
        )
        defs_parts.append(_gloss_gradient_defs(gloss_id))
        highlight_cx = x + _NAIL_WIDTH * 0.32
        highlight_cy = _TOP_Y + _NAIL_HEIGHT * 0.16
        body_parts.append(
            f'<g clip-path="url(#{clip_id})">'
            f'<rect x="{x:.1f}" y="{_TOP_Y:.1f}" width="{_NAIL_WIDTH}" height="{_NAIL_HEIGHT}" '
            f'fill="url(#{gloss_id})" />'
            f'<circle cx="{highlight_cx:.1f}" cy="{highlight_cy:.1f}" '
            f'r="{_NAIL_WIDTH * 0.16:.1f}" fill="#FFFFFF" opacity="0.55" '
            f'filter="url(#soft-blur)" />'
            f"</g>"
        )

    defs = f"<defs>{''.join(defs_parts)}</defs>"
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {_VIEW_WIDTH} {_VIEW_HEIGHT}">'
        f"{defs}{background_rect}{''.join(body_parts)}</svg>"
    )
