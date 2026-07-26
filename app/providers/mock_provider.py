"""決定的なモックAIプロバイダ(§6.2)。

設計理由: 実AI APIを呼ばずに「それらしい」提案3件を返す。`random.Random(seed)`
(グローバルな`random`ではなく専用インスタンス)を使うのは、同じseedなら
何度呼んでも同じ結果になることを保証するため。これによりキャッシュの
再現性テストや「同一入力ならAPI課金が発生しない」設計(§6.3)が検証できる。
"""

import random
import time
import uuid

from app.models.domain import Color, DesignProposal, DesignRequest, Scene, Style

# キー: Style。値: タイトルに使う語感パーツ(各10件程度、§6.2)。
TITLE_PARTS: dict[Style, list[str]] = {
    Style.simple: [
        "静けさ",
        "すっきり",
        "ミニマル",
        "無垢",
        "素直",
        "澄んだ",
        "軽やか",
        "柔らか",
        "端正",
        "静寂",
    ],
    Style.french: [
        "きちんと",
        "上品ライン",
        "クラシック",
        "王道フレンチ",
        "けじめ",
        "白い月",
        "清楚",
        "定番美人",
        "折り目正しい",
        "スタンダード",
    ],
    Style.gradient: [
        "ぼかし",
        "グラデーション",
        "溶けゆく",
        "淡いグラデ",
        "にじみ",
        "ぼんやり",
        "重なる色",
        "透明感グラデ",
        "移ろい",
        "溶け込む",
    ],
    Style.nuance: [
        "ニュアンス",
        "揺らぎ",
        "曖昧",
        "抽象",
        "アート",
        "気まぐれ",
        "重なる質感",
        "抜け感",
        "不完全美",
        "遊び心",
    ],
    Style.one_color: [
        "ワンカラー",
        "単色美人",
        "一色の潔さ",
        "シンプル一色",
        "モノトーン",
        "潔さ",
        "一色勝負",
        "純色",
        "無地の美",
        "一色の余白",
    ],
}

# キー: Scene。値: タイトルに使うシーン語。
SCENE_WORDS: dict[Scene, str] = {
    Scene.office: "オフィス",
    Scene.date: "デート",
    Scene.bridal: "ブライダル",
    Scene.event: "イベント",
    Scene.daily: "普段使い",
}

# キー: Scene。値: コンセプト文テンプレート。{palette_word}に配色の言葉を差し込む。
CONCEPT_TEMPLATES: dict[Scene, str] = {
    Scene.office: (
        "手元を上品に見せる{palette_word}配色。"
        "会議でも浮かない落ち着きと、ふとした瞬間の華やかさを両立します。"
    ),
    Scene.date: (
        "{palette_word}を纏った指先で、会うたびに印象が変わる特別感を演出します。"
        "相手の目に触れる瞬間を意識した配色です。"
    ),
    Scene.bridal: (
        "{palette_word}の清らかな輝きが、一生に一度の晴れ舞台の手元をやさしく彩ります。"
        "写真映えも計算した配色です。"
    ),
    Scene.event: (
        "{palette_word}で気分を高める、その日だけの特別なテンションを指先から。"
        "周りと差がつく華やかさを添えます。"
    ),
    Scene.daily: (
        "{palette_word}なら毎日でも飽きない気軽さ。家事や仕事の邪魔をしない、暮らしになじむ配色です。"
    ),
}

# キー: Style。値: 施術・セルフネイルのポイント(各10件程度、§6.2)。
POINT_POOL: dict[Style, list[str]] = {
    Style.simple: [
        "ベースは薄づき2度塗り",
        "トップは艶感重視",
        "はみ出しは先に修正しておく",
        "甘皮はしっかり処理する",
        "速乾トップで持ちを良くする",
        "爪先を丸く整えると欠けにくい",
        "色ムラは細筆で調整する",
        "1度塗りで様子を見る",
        "ライトは規定時間しっかり当てる",
        "指先の油分は事前に拭き取る",
    ],
    Style.french: [
        "ラインは細筆で一気に引く",
        "先端の白は2度塗りで発色",
        "ラインの角度は指に沿わせる",
        "ベースは薄いベージュで統一",
        "ラインテープを使うと均一になる",
        "先端の幅は3mm程度が上品",
        "ラインは中央から左右に引く",
        "白すぎない色でナチュラルに",
        "ラインの後はトップで一体化",
        "左右対称を意識して仕上げる",
    ],
    Style.gradient: [
        "根元は薄く、爪先へ濃く重ねる",
        "スポンジでぼかすとムラが出にくい",
        "色の境目は重ね塗りで馴染ませる",
        "2色を混ぜすぎず境界を残す",
        "乾く前にぼかすと綺麗に馴染む",
        "濃淡は3段階に分けて塗る",
        "スポンジは軽くポンポンと当てる",
        "はみ出しはあとで一括修正",
        "トップで色の境目を馴染ませる",
        "薄づきを重ねて濃度を調整する",
    ],
    Style.nuance: [
        "色は完全に混ぜず重ねて塗る",
        "筆先を叩くように色をのせる",
        "1色ずつ乾かしながら重ねる",
        "色数は2〜3色に抑える",
        "余白を作ると抜け感が出る",
        "同系色でまとめると失敗しにくい",
        "偶然の滲みも表情として活かす",
        "厚みが出過ぎないよう薄く重ねる",
        "指ごとに配色バランスを変える",
        "トップは厚めにのせて質感を統一",
    ],
    Style.one_color: [
        "ムラなく2〜3度塗りで発色",
        "筆に色を含ませすぎない",
        "端から塗ると綺麗に仕上がる",
        "1度塗りごとにしっかり乾かす",
        "トップジェルで艶を底上げする",
        "色選びは肌なじみを優先する",
        "はみ出しはウッドスティックで除去",
        "薄膜を意識して重ねる",
        "指先の丸みに沿って塗る",
        "仕上げに甘皮まわりを整える",
    ],
}

# キー: Color。値: ベースカラーのHEX(全10色)。
COLOR_BASE_HEX: dict[Color, str] = {
    Color.red: "#D1495B",
    Color.pink: "#F4B6C2",
    Color.beige: "#E8D3C0",
    Color.white: "#FFFFFF",
    Color.black: "#2B2B2B",
    Color.blue: "#5B7C99",
    Color.green: "#7A9E7E",
    Color.purple: "#9B7EBD",
    Color.gold: "#D4AF37",
    Color.silver: "#C0C0C0",
}

# キー: Color。値: タイトル・コンセプトに使う日本語の色名。
COLOR_JA_NAME: dict[Color, str] = {
    Color.red: "レッド",
    Color.pink: "ピンク",
    Color.beige: "ベージュ",
    Color.white: "ホワイト",
    Color.black: "ブラック",
    Color.blue: "ブルー",
    Color.green: "グリーン",
    Color.purple: "パープル",
    Color.gold: "ゴールド",
    Color.silver: "シルバー",
}

# 3案の方針(§6.2の4): 0=王道 / 1=遊びを1つ足す / 2=引き算。
_VARIANT_POINT_COUNTS = (3, 4, 2)
_VARIANT_SHADE_ADDITIONS = (1, 2, 0)

_MIN_PALETTE_SIZE = 3
_MAX_PALETTE_SIZE = 5
_LIGHTNESS_DELTA = 0.1


def _shift_lightness(hex_color: str, delta: float) -> str:
    """HEXカラーの明度をdelta分(例: +0.1で+10%)変化させた派生色を返す。"""
    r = int(hex_color[1:3], 16)
    g = int(hex_color[3:5], 16)
    b = int(hex_color[5:7], 16)
    factor = 1.0 + delta
    r, g, b = (min(255, max(0, round(channel * factor))) for channel in (r, g, b))
    return f"#{r:02X}{g:02X}{b:02X}"


class MockAIProvider:
    """テンプレートと乱数からデザイン提案を組み立てるモックプロバイダ。"""

    # RealAIProviderとの比較用。mockは実課金が発生しないため0.0固定(§6.3)。
    estimated_cost_usd_per_call: float = 0.0

    def __init__(self, latency_ms: int = 0) -> None:
        self._latency_ms = latency_ms

    def generate_designs(self, request: DesignRequest, seed: int) -> list[DesignProposal]:
        """条件に応じたデザイン提案を決定的に3件生成する。"""
        if self._latency_ms > 0:
            # 実API風の待ち時間を再現する(テストでは必ず0にして待たせない)。
            time.sleep(self._latency_ms / 1000)

        rng = random.Random(seed)
        scene_word = SCENE_WORDS[request.scene]
        concept_template = CONCEPT_TEMPLATES[request.scene]
        color_word = COLOR_JA_NAME[request.colors[0]]
        palette_word = "×".join(COLOR_JA_NAME[color] for color in request.colors)

        # 3案でタイトルの語感が重複しないよう、重複なしで3件を選ぶ。
        title_parts = rng.sample(TITLE_PARTS[request.style], k=3)

        proposals: list[DesignProposal] = []
        for variant in range(3):
            points = rng.sample(POINT_POOL[request.style], k=_VARIANT_POINT_COUNTS[variant])
            palette = self._build_palette(request.colors, rng, variant)

            concept = concept_template.format(palette_word=palette_word)
            if request.free_text:
                concept = f"{concept}ご要望の{request.free_text}も意識しました。"

            # uuid.uuid4()は乱数源がグローバルで決定的にできないため、
            # 同じseedで同じidになるようrngから128bitを取り出してUUIDを組み立てる。
            proposal_id = str(uuid.UUID(int=rng.getrandbits(128), version=4))
            proposals.append(
                DesignProposal(
                    id=proposal_id,
                    title=f"{scene_word}の{title_parts[variant]}{color_word}",
                    concept=concept,
                    palette=palette,
                    points=points,
                    image_url=f"/api/proposals/{proposal_id}/image.svg",
                )
            )
        return proposals

    @staticmethod
    def _build_palette(colors: list[Color], rng: random.Random, variant: int) -> list[str]:
        """選択色から3〜5色のパレットを組み立てる(§6.2の3)。"""
        palette = list(dict.fromkeys(COLOR_BASE_HEX[color] for color in colors))

        shade_additions = _VARIANT_SHADE_ADDITIONS[variant]
        needed_for_minimum = max(0, _MIN_PALETTE_SIZE - len(palette))
        add_count = max(shade_additions, needed_for_minimum)
        add_count = min(add_count, _MAX_PALETTE_SIZE - len(palette))

        for _ in range(add_count):
            source = rng.choice(palette)
            delta = rng.choice((-_LIGHTNESS_DELTA, _LIGHTNESS_DELTA))
            palette.append(_shift_lightness(source, delta))
        return palette
