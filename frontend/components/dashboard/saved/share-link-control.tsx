"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { inputClassName } from "@/components/ui/form-field";
import { useCreateShareLink, useRevokeShareLink } from "@/lib/hooks/use-saved-analyses";

function buildShareUrl(token: string): string {
  const base = process.env.NEXT_PUBLIC_SITE_URL ?? (typeof window !== "undefined" ? window.location.origin : "");
  return `${base}/shared/${token}`;
}

/**
 * 保存済み分析を未ログインの外部クライアント(代理店の顧客等)に閲覧専用で共有するボタン。
 * クリックすると共有リンクを発行(既に有効なリンクがあればそれを再利用)し、コピー・失効操作を提供する
 * (シニアレビュー: 「外部クライアント向けの閲覧専用共有リンクが無い」への対応)。
 */
export function ShareLinkControl({ savedAnalysisId }: { savedAnalysisId: string }) {
  const createMutation = useCreateShareLink();
  const revokeMutation = useRevokeShareLink();
  const [shareUrl, setShareUrl] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  const handleShare = async () => {
    const result = await createMutation.mutateAsync(savedAnalysisId);
    setShareUrl(buildShareUrl(result.token));
  };

  const handleCopy = async () => {
    if (!shareUrl) return;
    await navigator.clipboard.writeText(shareUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const handleRevoke = async () => {
    await revokeMutation.mutateAsync(savedAnalysisId);
    setShareUrl(null);
    setCopied(false);
  };

  if (shareUrl) {
    return (
      <div className="flex flex-wrap items-center gap-2">
        <input
          readOnly
          value={shareUrl}
          onFocus={(e) => e.currentTarget.select()}
          aria-label="共有リンク"
          className={`${inputClassName(false)} min-w-0 flex-1 font-mono text-xs`}
        />
        <Button variant="secondary" onClick={handleCopy}>
          {copied ? "コピーしました" : "コピー"}
        </Button>
        <Button variant="danger" onClick={handleRevoke} isLoading={revokeMutation.isPending}>
          共有を解除
        </Button>
      </div>
    );
  }

  return (
    <Button variant="secondary" onClick={handleShare} isLoading={createMutation.isPending}>
      🔗 共有リンクを発行
    </Button>
  );
}
