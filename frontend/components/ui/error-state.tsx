import { ApiError } from "@/lib/types/common";

interface ErrorStateProps {
  error?: unknown;
  title?: string;
  onRetry?: () => void;
}

/**
 * API取得エラーを表示する共通コンポーネント。
 * バックエンド未起動時（ApiError.status === 0）は分かりやすい専用メッセージを出す。
 */
export function ErrorState({ error, title, onRetry }: ErrorStateProps) {
  const isApiError = error instanceof ApiError;
  const isOffline = isApiError && error.status === 0;

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
