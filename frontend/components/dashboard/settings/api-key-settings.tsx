"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { QueryState } from "@/components/dashboard/query-state";
import { useApiKeySettings, useRegenerateApiKey } from "@/lib/hooks/use-settings";

export function ApiKeySettingsPanel() {
  const query = useApiKeySettings();
  const regenerateMutation = useRegenerateApiKey();
  const [isRevealed, setIsRevealed] = useState(false);

  return (
    <QueryState
      isLoading={query.isLoading}
      isError={query.isError}
      error={query.error}
      onRetry={() => query.refetch()}
    >
      <div className="space-y-3">
        <div className="flex items-center gap-2 rounded-lg border border-slate-100 bg-slate-50 px-3 py-2 font-mono text-sm">
          <span className="flex-1 truncate">
            {query.data?.apiKey
              ? isRevealed
                ? query.data.apiKey
                : "•".repeat(24)
              : "APIキーはまだ発行されていません"}
          </span>
          {query.data?.apiKey && (
            <button
              type="button"
              onClick={() => setIsRevealed((v) => !v)}
              className="text-xs font-medium text-brand-600 hover:underline"
            >
              {isRevealed ? "隠す" : "表示"}
            </button>
          )}
        </div>
        <Button
          variant="secondary"
          onClick={() => regenerateMutation.mutate()}
          isLoading={regenerateMutation.isPending}
        >
          APIキーを再発行
        </Button>
        <p className="text-xs text-slate-400">
          再発行すると、既存のAPIキーは無効になります。連携中のツールがある場合はご注意ください。
        </p>
      </div>
    </QueryState>
  );
}
