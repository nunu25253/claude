"""CostGuard / BudgetPolicy / BudgetExceededError(§6.3)。

設計理由: 実AI接続時に課金が発生する処理(Provider呼び出し)の入り口を
`CostGuard.call()` の1本に絞ることで、「呼び出し漏れで上限チェックを
すり抜ける」事故を構造的に防ぐ。呼び出し側はProviderを直接呼んではならない。
"""

from collections.abc import Callable
from dataclasses import dataclass
from datetime import date

from app.models.domain import DesignProposal, DesignRequest
from app.providers.base import AIProvider


class BudgetExceededError(Exception):
    """本日の呼び出し回数またはコストの上限を超えた場合に送出する。"""


@dataclass
class BudgetPolicy:
    """1日あたりの呼び出し上限(§6.3)。"""

    max_calls_per_day: int
    max_cost_usd_per_day: float


class CostGuard:
    """Provider呼び出しを一元管理し、1日ごとの予算を強制するガード。"""

    def __init__(
        self,
        policy: BudgetPolicy,
        today_fn: Callable[[], date] = date.today,
    ) -> None:
        self._policy = policy
        # 日付跨ぎをテストで再現できるよう、"今日"を関数として注入する(§6.3)。
        self._today_fn = today_fn
        self._current_day: date | None = None
        self._calls_today = 0
        self._cost_today_usd = 0.0

    def _reset_if_new_day(self) -> None:
        today = self._today_fn()
        if today != self._current_day:
            # カウンタはプロセスのメモリ上にのみ保持する。プロセス再起動でも
            # リセットされてしまうが、プロトタイプの範囲では許容する(§6.3)。
            self._current_day = today
            self._calls_today = 0
            self._cost_today_usd = 0.0

    def call(self, provider: AIProvider, request: DesignRequest, seed: int) -> list[DesignProposal]:
        """予算内であればProviderを呼び出し、結果を返す。超過時は例外を送出。"""
        self._reset_if_new_day()

        cost_per_call = provider.estimated_cost_usd_per_call
        if self._calls_today + 1 > self._policy.max_calls_per_day:
            raise BudgetExceededError("本日の生成回数の上限に達しました。")
        if self._cost_today_usd + cost_per_call > self._policy.max_cost_usd_per_day:
            raise BudgetExceededError("本日のコスト上限に達しました。")

        self._calls_today += 1
        self._cost_today_usd += cost_per_call
        return provider.generate_designs(request, seed)

    @property
    def calls_today(self) -> int:
        """本日すでに実行した呼び出し回数。"""
        self._reset_if_new_day()
        return self._calls_today

    @property
    def estimated_cost_today_usd(self) -> float:
        """本日の推定コスト(USD)。"""
        self._reset_if_new_day()
        return self._cost_today_usd
