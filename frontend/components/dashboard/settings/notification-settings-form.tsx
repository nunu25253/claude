"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { QueryState } from "@/components/dashboard/query-state";
import {
  useNotificationSettings,
  useUpdateNotificationSettings,
} from "@/lib/hooks/use-settings";
import type { NotificationSettings } from "@/lib/types";

const DEFAULT_SETTINGS: NotificationSettings = {
  emailOnAnalysisComplete: true,
  emailWeeklyDigest: false,
  emailTrendingAlert: false,
};

const ITEMS: { key: keyof NotificationSettings; label: string; description: string }[] = [
  {
    key: "emailOnAnalysisComplete",
    label: "分析完了通知",
    description: "投稿分析が完了したときにメールで通知します",
  },
  {
    key: "emailWeeklyDigest",
    label: "週次ダイジェスト",
    description: "週間ランキングのサマリーをメールで受け取ります",
  },
  {
    key: "emailTrendingAlert",
    label: "急上昇アラート",
    description: "フォロー中ジャンルで急上昇投稿が出た際に通知します",
  },
];

export function NotificationSettingsForm() {
  const query = useNotificationSettings();
  const updateMutation = useUpdateNotificationSettings();
  const [values, setValues] = useState<NotificationSettings>(DEFAULT_SETTINGS);

  useEffect(() => {
    if (query.data) setValues(query.data);
  }, [query.data]);

  const toggle = (key: keyof NotificationSettings) => {
    setValues((prev) => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <QueryState
      isLoading={query.isLoading}
      isError={query.isError}
      error={query.error}
      onRetry={() => query.refetch()}
    >
      <div className="space-y-4">
        {ITEMS.map((item) => (
          <label
            key={item.key}
            className="flex cursor-pointer items-start gap-3 rounded-lg border border-slate-100 p-3 hover:bg-slate-50"
          >
            <input
              type="checkbox"
              className="mt-0.5 h-4 w-4 rounded border-slate-300 text-brand-600 focus:ring-brand-400"
              checked={values[item.key]}
              onChange={() => toggle(item.key)}
            />
            <span>
              <span className="block text-sm font-medium text-slate-700">{item.label}</span>
              <span className="block text-xs text-slate-400">{item.description}</span>
            </span>
          </label>
        ))}

        <div className="flex items-center gap-3">
          <Button
            onClick={() => updateMutation.mutate(values)}
            isLoading={updateMutation.isPending}
          >
            保存する
          </Button>
          {updateMutation.isSuccess && (
            <span className="text-sm text-emerald-600">保存しました</span>
          )}
          {updateMutation.isError && (
            <span className="text-sm text-red-500">保存に失敗しました</span>
          )}
        </div>
      </div>
    </QueryState>
  );
}
