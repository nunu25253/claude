import { LoadingState } from "@/components/ui/loading-state";
import { ErrorState } from "@/components/ui/error-state";
import { EmptyState } from "@/components/ui/empty-state";

interface QueryStateProps {
  isLoading: boolean;
  isError: boolean;
  error?: unknown;
  isEmpty?: boolean;
  emptyTitle?: string;
  emptyDescription?: string;
  onRetry?: () => void;
  children: React.ReactNode;
}

/**
 * React Query の loading / error / empty / success 状態をまとめて出し分ける共通コンポーネント。
 * 各ページはこれで包むだけでバックエンド未起動時のフォールバック表示まで一貫させられる。
 */
export function QueryState({
  isLoading,
  isError,
  error,
  isEmpty,
  emptyTitle = "データがありません",
  emptyDescription,
  onRetry,
  children,
}: QueryStateProps) {
  if (isLoading) return <LoadingState />;
  if (isError) return <ErrorState error={error} onRetry={onRetry} />;
  if (isEmpty) return <EmptyState title={emptyTitle} description={emptyDescription} />;
  return <>{children}</>;
}
