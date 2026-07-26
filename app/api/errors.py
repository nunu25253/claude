"""共通エラーレスポンス変換(§8)。

設計理由: どのエンドポイントで例外が起きても
`{"error": {"code": ..., "message": ...}}` という同じ形で返したい。個々の
ルートでtry/exceptを書くと書き漏れが起きるため、FastAPIの例外ハンドラとして
一箇所に集約する。
"""

import logging

from fastapi import FastAPI, Request, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from app.core.cost_guard import BudgetExceededError
from app.core.security import FreeTextTooLongError, SuspiciousInputError

logger = logging.getLogger(__name__)


class NotFoundError(Exception):
    """指定されたリソースが存在しない場合に送出する。"""


def _error_response(status_code: int, code: str, message: str) -> JSONResponse:
    """§8で固定されたエラーレスポンス形式を組み立てる。"""
    return JSONResponse(
        status_code=status_code, content={"error": {"code": code, "message": message}}
    )


def register_exception_handlers(app: FastAPI) -> None:
    """§8のフォーマットに変換する例外ハンドラをまとめて登録する。"""

    @app.exception_handler(RequestValidationError)
    def handle_validation_error(request: Request, exc: RequestValidationError) -> JSONResponse:
        return _error_response(
            status.HTTP_422_UNPROCESSABLE_CONTENT,
            "validation_error",
            "入力内容を確認してください。",
        )

    @app.exception_handler(FreeTextTooLongError)
    def handle_free_text_too_long(request: Request, exc: FreeTextTooLongError) -> JSONResponse:
        return _error_response(status.HTTP_422_UNPROCESSABLE_CONTENT, "validation_error", str(exc))

    @app.exception_handler(SuspiciousInputError)
    def handle_suspicious_input(request: Request, exc: SuspiciousInputError) -> JSONResponse:
        return _error_response(status.HTTP_422_UNPROCESSABLE_CONTENT, "suspicious_input", str(exc))

    @app.exception_handler(BudgetExceededError)
    def handle_budget_exceeded(request: Request, exc: BudgetExceededError) -> JSONResponse:
        return _error_response(
            status.HTTP_429_TOO_MANY_REQUESTS,
            "budget_exceeded",
            "本日の生成上限に達しました。明日また試してください。",
        )

    @app.exception_handler(NotFoundError)
    def handle_not_found(request: Request, exc: NotFoundError) -> JSONResponse:
        return _error_response(status.HTTP_404_NOT_FOUND, "not_found", str(exc))

    @app.exception_handler(Exception)
    def handle_internal_error(request: Request, exc: Exception) -> JSONResponse:
        logger.exception("internal server error")
        return _error_response(
            status.HTTP_500_INTERNAL_SERVER_ERROR,
            "internal",
            "サーバー内部でエラーが発生しました。",
        )
