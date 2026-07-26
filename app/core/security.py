"""プロンプトインジェクション対策(§6.5)。

設計理由: 実AI接続はスコープ外(Phase 5でスタブのみ)だが、無害化処理を
Mock段階のうちから作っておく。実接続時に後付けすると「動いているから」と
チェックが漏れやすいため、最初から全経路(design_service経由)を通す。
"""

import hashlib
import logging
import re
import unicodedata

logger = logging.getLogger(__name__)

MAX_FREE_TEXT_LENGTH = 120
WARNING_LOG_PREVIEW_LENGTH = 50


class SecurityValidationError(Exception):
    """sanitize_free_textが入力を拒否する際の基底例外。"""


class SuspiciousInputError(SecurityValidationError):
    """§6.5の禁止パターン(指示上書き・URL等)に一致した場合に送出する。"""


class FreeTextTooLongError(SecurityValidationError):
    """正規化後の自由記述が120文字を超えた場合に送出する。"""


# 大文字小文字を無視した部分一致で検知する(§6.5)。
SUSPICIOUS_PATTERNS: list[re.Pattern[str]] = [
    re.compile(r"ignore\s+(all|previous|above)", re.IGNORECASE),
    re.compile(r"以前の指示", re.IGNORECASE),
    re.compile(r"指示を無視", re.IGNORECASE),
    re.compile(r"無視して", re.IGNORECASE),
    re.compile(r"system\s*prompt", re.IGNORECASE),
    re.compile(r"システムプロンプト", re.IGNORECASE),
    re.compile(r"<system>", re.IGNORECASE),
    re.compile(r"```"),
    re.compile(r"you are now", re.IGNORECASE),
    re.compile(r"あなたは今から", re.IGNORECASE),
    re.compile(r"assistant\s*:", re.IGNORECASE),
    re.compile(r"https?://", re.IGNORECASE),
]


def _strip_control_chars(text: str) -> str:
    """Unicodeカテゴリ Cc(制御文字)を除去する。"""
    return "".join(char for char in text if unicodedata.category(char) != "Cc")


def _log_suspicious(raw: str) -> None:
    """検知した入力を先頭50文字とSHA256で記録する(生の全文はログに残さない)。"""
    preview = raw[:WARNING_LOG_PREVIEW_LENGTH]
    digest = hashlib.sha256(raw.encode("utf-8")).hexdigest()
    logger.warning("suspicious free_text detected: preview=%r sha256=%s", preview, digest)


def sanitize_free_text(raw: str) -> str:
    """自由記述を無害化して返す。危険な入力は例外を送出して拒否する。

    手順(§6.5で固定): 制御文字除去 → NFKC正規化 → strip →
    120文字超チェック → 禁止パターンチェック。
    """
    without_control = _strip_control_chars(raw)
    normalized = unicodedata.normalize("NFKC", without_control).strip()

    if len(normalized) > MAX_FREE_TEXT_LENGTH:
        raise FreeTextTooLongError("自由記述は120文字以内で入力してください。")

    for pattern in SUSPICIOUS_PATTERNS:
        if pattern.search(normalized):
            _log_suspicious(raw)
            raise SuspiciousInputError("入力内容を確認してください。")

    return normalized
