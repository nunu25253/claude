"""§5で固定されたPydanticドメインモデル。

設計理由: フィールド名・型はフロントエンド(app.js)とテストの両方が前提にする
「契約」なので、ここを勝手に変えると全体が壊れる。そのため本ファイルは
IMPLEMENTATION_PROMPT.md §5の記述をそのまま実装する(独自の追加フィールドや
リネームをしない)。`(str, Enum)` の多重継承もPydantic v2の慣例に合わせた
§5の記述通りの形であり、ruffの`StrEnum`推奨(UP042)は本ファイルでは無視する。
"""

# ruff: noqa: UP042
from enum import Enum

from pydantic import BaseModel, Field


class Scene(str, Enum):
    """デザインを使う場面。"""

    office = "office"
    date = "date"
    bridal = "bridal"
    event = "event"
    daily = "daily"


class Shape(str, Enum):
    """爪の形。"""

    round = "round"
    oval = "oval"
    square = "square"
    almond = "almond"


class Length(str, Enum):
    """爪の長さ。"""

    short = "short"
    medium = "medium"
    long = "long"


class Style(str, Enum):
    """ネイルのスタイル。"""

    simple = "simple"
    french = "french"
    gradient = "gradient"
    nuance = "nuance"
    one_color = "one_color"


class Color(str, Enum):
    """基調となる色。"""

    red = "red"
    pink = "pink"
    beige = "beige"
    white = "white"
    black = "black"
    blue = "blue"
    green = "green"
    purple = "purple"
    gold = "gold"
    silver = "silver"


class DesignRequest(BaseModel):
    """デザイン生成リクエスト(§2.2の入力項目)。"""

    scene: Scene
    colors: list[Color] = Field(min_length=1, max_length=3)
    shape: Shape
    length: Length
    style: Style
    free_text: str | None = Field(default=None, max_length=120)


class DesignProposal(BaseModel):
    """1件のデザイン提案(§2.3の出力)。"""

    id: str  # uuid4
    title: str
    concept: str
    palette: list[str]  # "#RRGGBB" 3〜5色
    points: list[str]  # 2〜4項目
    image_url: str  # "/api/proposals/{id}/image.svg"


class GenerateResponse(BaseModel):
    """`POST /api/designs/generate` のレスポンス。"""

    generation_id: str
    cache_hit: bool
    proposals: list[DesignProposal]  # 常に3件
