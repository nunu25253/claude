"""AIProvider抽象化。

設計理由: Protocol(構造的部分型。継承なしでも「このメソッドがあれば型として
認める」という仕組み)にするのは、MockAIProviderとRealAIProviderが継承関係を
持たなくても同じ型として扱えるようにするため。CostGuardやdesign_serviceは
この型だけを知っていればよく、具体的な実装(mock/real)を意識しなくてよい。
"""

from typing import Protocol

from app.models.domain import DesignProposal, DesignRequest


class AIProvider(Protocol):
    """デザイン提案を生成するプロバイダの共通インタフェース。"""

    estimated_cost_usd_per_call: float

    def generate_designs(self, request: DesignRequest, seed: int) -> list[DesignProposal]:
        """条件に応じたデザイン提案を3件生成する。"""
        ...
