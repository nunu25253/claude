"""実AIプロバイダのスタブ(§6.1)。実接続はスコープ外(§2.4)。

`AI_PROVIDER=real`に切り替えるとこのクラスが選ばれるが、呼び出すと
`NotImplementedError`になる。将来実装する際は、
`app.services.design_service.build_prompt()`で組み立てたプロンプトを使い、
ユーザー入力を必ず`<user_input>`タグの中の「データ」として扱うこと(§6.5)。
"""

from app.models.domain import DesignProposal, DesignRequest


class RealAIProvider:
    """実AI API(OpenAI/Anthropic等)へ接続するプロバイダ(未実装のスタブ)。"""

    # 実課金が発生する前提の単価(§6.3)。実際の単価は接続先に合わせて調整する。
    estimated_cost_usd_per_call: float = 0.02

    def generate_designs(self, request: DesignRequest, seed: int) -> list[DesignProposal]:
        """実AI APIを呼び出してデザイン提案を得る(Phase外につき未実装)。"""
        # TODO: ここで実際のAI APIを呼び出す。build_prompt()で組み立てた
        # プロンプトをそのまま送り、レスポンスをDesignProposalへ変換する。
        raise NotImplementedError("Phase外。実接続時に実装")
