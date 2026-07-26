"""SQLAlchemyモデル(§7)とセッション管理。

設計理由: 生SQL禁止(§3、将来PostgreSQLへ移行するため)なので、テーブル定義と
セッション生成をORM経由のここに集約する。SQLiteは`check_same_thread=False`
にしないと、FastAPIの同期エンドポイント(スレッドプールで実行される)から
接続を使い回すときにエラーになるため明示的に指定する。
"""

from datetime import datetime

from sqlalchemy import ForeignKey, create_engine
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column, sessionmaker
from sqlalchemy.pool import StaticPool


class Base(DeclarativeBase):
    """全テーブル共通の基底クラス。"""


class GenerationRow(Base):
    """§7の`generations`テーブル。"""

    __tablename__ = "generations"

    id: Mapped[str] = mapped_column(primary_key=True)
    request_json: Mapped[str]
    cache_key: Mapped[str] = mapped_column(index=True)
    created_at: Mapped[datetime]


class ProposalRow(Base):
    """§7の`proposals`テーブル。"""

    __tablename__ = "proposals"

    id: Mapped[str] = mapped_column(primary_key=True)
    generation_id: Mapped[str] = mapped_column(ForeignKey("generations.id"))
    title: Mapped[str]
    concept: Mapped[str]
    palette_json: Mapped[str]
    points_json: Mapped[str]
    sort_order: Mapped[int]


class FavoriteRow(Base):
    """§7の`favorites`テーブル。"""

    __tablename__ = "favorites"

    id: Mapped[int] = mapped_column(primary_key=True, autoincrement=True)
    proposal_id: Mapped[str] = mapped_column(ForeignKey("proposals.id"), unique=True)
    created_at: Mapped[datetime]


def create_session_factory(database_url: str) -> sessionmaker[Session]:
    """DATABASE_URLからテーブル作成済みのセッションファクトリを作る。

    設計理由: `:memory:`のSQLiteは接続ごとに別DBになってしまうため、
    テスト用に単一コネクションを使い回す`StaticPool`を使う。ファイルDBでは
    通常のプール(接続ごとにテーブルが共有される)で問題ない。
    """
    is_memory_db = database_url.endswith(":memory:")
    engine = create_engine(
        database_url,
        connect_args={"check_same_thread": False},
        poolclass=StaticPool if is_memory_db else None,
    )
    Base.metadata.create_all(engine)
    return sessionmaker(bind=engine, expire_on_commit=False)
