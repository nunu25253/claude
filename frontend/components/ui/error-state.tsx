import { useState } from "react";
import Link from "next/link";
import { authApi } from "@/lib/api";
import { useAuth } from "@/lib/auth/auth-context";
import { ApiError } from "@/lib/types/common";

/** バックエンドがAI機能の日次利用上限超過時に付与するerrorCode(UsageQuotaService.EXCEEDED_ERROR_CODEと対応) */
const AI_USAGE_QUOTA_EXCEEDED_CODE = "AI_USAGE_QUOTA_EXCEEDED";

/**
 * バックエンドがメール未確認かつ無料お試し分析を既に使い切った場合に付与するerrorCode
 * (PostAnalysisApplicationService.EMAIL_NOT_VERIFIED_CODEと対応)。
 * 1回目はサーバー側で自動的にお試し扱いになるため、このエラーは2回目以降の未確認ユーザーにのみ発生する。
 */
const EMAIL_NOT_VERIFIED_CODE = "EMAIL_NOT_VERIFIED";

interface ErrorStateProps {
  error?: unknown;
  title?: string;
  onRetry?: () => void;
}

/**
 * API取得エラーを表示する共通コンポーネント。
 * バックエンド未起動時（ApiError.status === 0）は分かりやすい専用メッセージを出す。
 * AI機能の日次利用上限超過時（ApiError.code === AI_USAGE_QUOTA_EXCEEDED_CODE）は
 * 汎用エラー表示の代わりにPROプランへのアップセルCTAを出す
 * （メッセージ文字列の一致ではなくcodeで判定するため、文言変更に影響されない）。
 */
export function ErrorState({ error, title, onRetry }: ErrorStateProps) {
  const { user } = useAuth();
  const [resendState, setResendState] = useState<"idle" | "sending" | "sent">("idle");
  const isApiError = error instanceof ApiError;
  const isOffline = isApiError && error.status === 0;
  const isQuotaExceeded = isApiError && error.code === AI_USAGE_QUOTA_EXCEEDED_CODE;
  const isEmailNotVerified = isApiError && error.code === EMAIL_NOT_VERIFIED_CODE;

  const handleResend = async () => {
    if (!user) return;
    setResendState("sending");
    try {
      await authApi.resendEmailVerification({ email: user.email });
      setResendState("sent");
    } catch {
      setResendState("idle");
    }
  };

  if (isEmailNotVerified) {
    return (
      <div
        role="alert"
        className="flex flex-col items-center gap-3 rounded-2xl border border-amber-200 bg-amber-50 px-6 py-10 text-center dark:border-amber-800 dark:bg-amber-950"
      >
        <p className="font-semibold text-amber-800 dark:text-amber-300">
          無料お試し分析は既にご利用いただきました
        </p>
        <p className="max-w-md text-sm text-amber-700 dark:text-amber-400">
          引き続きご利用いただくには、メールアドレスの確認が必要です。確認メールのリンクからご確認ください。
        </p>
        {resendState === "sent" ? (
          <span className="mt-2 text-sm font-medium text-amber-800 dark:text-amber-300">
            確認メールを再送しました
          </span>
        ) : (
          <button
            type="button"
            onClick={handleResend}
            disabled={resendState === "sending"}
            className="btn-primary mt-2 disabled:opacity-50"
          >
            確認メールを再送する
          </button>
        )}
      </div>
    );
  }

  if (isQuotaExceeded) {
    return (
      <div
        role="alert"
        className="flex flex-col items-center gap-3 rounded-2xl border border-brand-200 bg-brand-50 px-6 py-10 text-center dark:border-brand-800 dark:bg-brand-900"
      >
        <div className="text-2xl">🚀</div>
        <p className="font-semibold text-brand-700 dark:text-brand-300">
          本日のAI機能の利用回数上限に達しました
        </p>
        <p className="max-w-md text-sm text-brand-600 dark:text-brand-400">
          {error.message}
          {" "}PROプランにアップグレードすると、1日あたりの利用回数上限が広がります。
        </p>
        <Link href="/billing" className="btn-primary mt-2">
          プランを見る
        </Link>
      </div>
    );
  }

  const message = isApiError
    ? error.message
    : "予期しないエラーが発生しました。";

  return (
    <div
      role="alert"
      className="flex flex-col items-center gap-3 rounded-2xl border border-red-200 bg-red-50 px-6 py-10 text-center"
    >
      <div className="text-2xl">{isOffline ? "🔌" : "⚠️"}</div>
      <p className="font-semibold text-red-700">
        {title ?? (isOffline ? "APIサーバーに接続できません" : "データの取得に失敗しました")}
      </p>
      <p className="max-w-md text-sm text-red-600">{message}</p>
      {isOffline && (
        <p className="max-w-md text-xs text-red-500">
          バックエンド（Spring Boot API）が起動しているか、NEXT_PUBLIC_API_BASE_URL の設定を確認してください。
        </p>
      )}
      {onRetry && (
        <button type="button" onClick={onRetry} className="btn-secondary mt-2">
          再試行
        </button>
      )}
    </div>
  );
}
