"""お気に入りのAPIルーター(§8)。"""

import json
from datetime import UTC, datetime

from fastapi import APIRouter, Depends, Response, status
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.api.deps import get_db
from app.api.errors import NotFoundError
from app.models.db import FavoriteRow, ProposalRow
from app.models.domain import DesignProposal

router = APIRouter(prefix="/api/favorites", tags=["favorites"])


class FavoriteCreateRequest(BaseModel):
    """`POST /api/favorites`のリクエストボディ。"""

    proposal_id: str


def _to_domain_proposal(row: ProposalRow) -> DesignProposal:
    """DBの行をAPIレスポンス用のDesignProposalへ変換する。"""
    return DesignProposal(
        id=row.id,
        title=row.title,
        concept=row.concept,
        palette=json.loads(row.palette_json),
        points=json.loads(row.points_json),
        image_url=f"/api/proposals/{row.id}/image.svg",
    )


@router.post("")
def add_favorite(
    payload: FavoriteCreateRequest,
    response: Response,
    db: Session = Depends(get_db),
) -> dict[str, str]:
    """お気に入りに登録する。既に登録済みなら200を返す(冪等、§8)。"""
    proposal = db.get(ProposalRow, payload.proposal_id)
    if proposal is None:
        raise NotFoundError("指定されたデザイン提案が見つかりません。")

    existing = db.execute(
        select(FavoriteRow).where(FavoriteRow.proposal_id == payload.proposal_id)
    ).scalar_one_or_none()
    if existing is not None:
        response.status_code = status.HTTP_200_OK
        return {"proposal_id": payload.proposal_id}

    db.add(FavoriteRow(proposal_id=payload.proposal_id, created_at=datetime.now(UTC)))
    db.commit()
    response.status_code = status.HTTP_201_CREATED
    return {"proposal_id": payload.proposal_id}


@router.delete("/{proposal_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_favorite(proposal_id: str, db: Session = Depends(get_db)) -> Response:
    """お気に入りから削除する。未登録なら404(§8)。"""
    existing = db.execute(
        select(FavoriteRow).where(FavoriteRow.proposal_id == proposal_id)
    ).scalar_one_or_none()
    if existing is None:
        raise NotFoundError("お気に入りが見つかりません。")

    db.delete(existing)
    db.commit()
    return Response(status_code=status.HTTP_204_NO_CONTENT)


@router.get("", response_model=list[DesignProposal])
def list_favorites(db: Session = Depends(get_db)) -> list[DesignProposal]:
    """お気に入りのproposalを新しい順で返す(§8)。"""
    rows = (
        db.execute(
            select(ProposalRow)
            .join(FavoriteRow, FavoriteRow.proposal_id == ProposalRow.id)
            .order_by(FavoriteRow.created_at.desc())
        )
        .scalars()
        .all()
    )
    return [_to_domain_proposal(row) for row in rows]
