"use client";

import { useState } from "react";
import { useSetAlertThreshold } from "@/lib/hooks/use-saved-analyses";
import { Button } from "@/components/ui/button";
import { inputClassName } from "@/components/ui/form-field";
import type { SavedAnalysis } from "@/lib/types";

export function AlertThresholdControl({ item }: { item: SavedAnalysis }) {
  const [draft, setDraft] = useState("");
  const mutation = useSetAlertThreshold();

  const handleSet = () => {
    const threshold = Number(draft);
    if (Number.isNaN(threshold) || threshold < 0 || threshold > 100) return;
    mutation.mutate({ id: item.id, payload: { threshold } });
  };

  const handleClear = () => {
    mutation.mutate({ id: item.id, payload: { threshold: null } });
  };

  if (item.alertThreshold === null) {
    return (
      <div className="mt-2 flex items-center gap-2">
        <input
          type="number"
          min={0}
          max={100}
          placeholder="しきい値(0-100)"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          className={`${inputClassName(false)} w-32 py-1 text-xs`}
        />
        <Button variant="secondary" onClick={handleSet} isLoading={mutation.isPending} className="px-2 py-1 text-xs">
          🔔 アラート設定
        </Button>
      </div>
    );
  }

  return (
    <div className="mt-2 flex items-center gap-2 text-xs">
      <span className="rounded-full bg-amber-50 px-2 py-0.5 font-medium text-amber-700 dark:bg-amber-950 dark:text-amber-300">
        BuzzScore {item.alertThreshold}以上で通知
      </span>
      <span className="text-slate-500 dark:text-slate-400">
        {item.alertTriggeredAt ? "通知済み" : "未通知"}
      </span>
      <button
        type="button"
        onClick={handleClear}
        disabled={mutation.isPending}
        className="text-slate-500 underline hover:text-slate-600 disabled:opacity-50 dark:text-slate-400"
      >
        解除
      </button>
    </div>
  );
}
