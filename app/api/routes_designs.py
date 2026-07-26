"""デザイン生成・履歴・画像のAPIルーター(§8)。"""

import hashlib
import json
from datetime import datetime

from fastapi import APIRouter, Depends, Response
from pydantic import BaseModel
from sqlalchemy import select
from sqlalchemy.orm import Session

from app.api.deps import get_db, get_design_service
from app.api.errors import NotFoundError
from app.models.db import GenerationRow, ProposalRow
from app.models.domain import DesignProposal, DesignRequest, GenerateResponse, Shape, Style
from app.services.design_service import DesignService
from app.services.image_service import render_nail_image

router = APIRouter(prefix="/api/designs", tags=["designs"])
# proposals/{id}/image.svg は /api/designs のprefixに乗らないため別ルーターにする。
image_router = APIRouter(prefix="/api/proposals", tags=["designs"])

# §8: historyのlimitはこの値で頭打ちにする。
_MAX_HISTORY_LIMIT = 50


class HistoryItem(BaseModel):
    """履歴1件分(1回の生成イベントと、その3案)。"""

    generation_id: str
    created_at: datetime
    proposals: list[DesignProposal]


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


@router.post("/generate", response_model=GenerateResponse)
def generate_designs(
    request: DesignRequest,
    db: Session = Depends(get_db),
    service: DesignService = Depends(get_design_service),
) -> GenerateResponse:
    """条件からデザイン提案を3件生成する(§8)。"""
    result = service.generate(request, db=db)
    return GenerateResponse(
        generation_id=result.generation_id,
        cache_hit=result.cache_hit,
        proposals=result.proposals,
    )


@router.get("/history", response_model=list[HistoryItem])
def get_history(
    limit: int = 20,
    offset: int = 0,
    db: Session = Depends(get_db),
) -> list[HistoryItem]:
    """生成履歴を新しい順に返す(limitは最大50件、§8)。"""
    capped_limit = min(limit, _MAX_HISTORY_LIMIT)
    generation_rows = (
        db.execute(
            select(GenerationRow)
            .order_by(GenerationRow.created_at.desc())
            .limit(capped_limit)
            .offset(offset)
        )
        .scalars()
        .all()
    )

    items: list[HistoryItem] = []
    for generation_row in generation_rows:
        proposal_rows = (
            db.execute(
                select(ProposalRow)
                .where(ProposalRow.generation_id == generation_row.id)
                .order_by(ProposalRow.sort_order)
            )
            .scalars()
            .all()
        )
        items.append(
            HistoryItem(
                generation_id=generation_row.id,
                created_at=generation_row.created_at,
                proposals=[_to_domain_proposal(row) for row in proposal_rows],
            )
        )
    return items


@image_router.get("/{proposal_id}/image.svg")
def get_proposal_image(proposal_id: str, db: Session = Depends(get_db)) -> Response:
    """proposalの条件から組み立てたモックSVG画像を返す(§9)。存在しなければ404。"""
    proposal_row = db.get(ProposalRow, proposal_id)
    if proposal_row is None:
        raise NotFoundError("指定されたデザイン提案が見つかりません。")

    generation_row = db.get(GenerationRow, proposal_row.generation_id)
    # generations.idへの外部キーがあるため、proposalが存在すれば必ず見つかる。
    assert generation_row is not None
    request_data = json.loads(generation_row.request_json)

    # proposal_idはSVGの見た目にだけ影響する乱数のseedにする(同一idなら同一画像)。
    seed = int(hashlib.sha256(proposal_id.encode("utf-8")).hexdigest()[:16], 16)
    svg = render_nail_image(
        palette=json.loads(proposal_row.palette_json),
        shape=Shape(request_data["shape"]),
        style=Style(request_data["style"]),
        seed=seed,
    )
    return Response(
        content=svg,
        media_type="image/svg+xml",
        headers={"Cache-Control": "max-age=86400"},
    )
