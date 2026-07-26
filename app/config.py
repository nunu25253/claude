"""アプリケーション設定。

設計理由: マジックナンバー禁止(§4)のため、環境変数由来の値はすべてここに集約する。
pydantic-settings を使うのは、型検証つきで .env / 環境変数を1箇所から読めるため
(バラバラに os.environ.get() するとtypoや型ミスに気づけない)。
"""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """§14 の .env.example に対応する設定値。"""

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    ai_provider: str = "mock"
    mock_latency_ms: int = 0

    cache_max_size: int = 256
    cache_ttl_seconds: int = 3600

    budget_max_calls_per_day: int = 200
    budget_max_cost_usd_per_day: float = 1.00

    database_url: str = "sqlite:///data/nailmuse.db"
