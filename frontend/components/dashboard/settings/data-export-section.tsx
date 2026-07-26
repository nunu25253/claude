"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { authApi } from "@/lib/api";
import { downloadJson } from "@/lib/download";
import { ApiError } from "@/lib/types/common";

export function DataExportSection() {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleExport = async () => {
    setError(null);
    setIsSubmitting(true);
    try {
      const data = await authApi.exportAccountData();
      const today = new Date().toISOString().slice(0, 10);
      downloadJson(`buzzly-data-export-${today}.json`, data);
    } catch (err) {
      setError(
        err instanceof ApiError
          ? err.message
          : "データのエクスポートに失敗しました。時間をおいて再度お試しください。",
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-3">
      <p className="text-sm text-slate-500 dark:text-slate-400">
        プロフィール・通知設定・保存済み分析・チーム所属・購読状況・レポート履歴をJSON形式で
        ダウンロードします。
      </p>
      {error && (
        <p className="text-sm text-red-600 dark:text-red-400" role="alert">
          {error}
        </p>
      )}
      <Button variant="secondary" onClick={handleExport} isLoading={isSubmitting}>
        データをダウンロード
      </Button>
    </div>
  );
}
