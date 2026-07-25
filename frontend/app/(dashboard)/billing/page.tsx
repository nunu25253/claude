"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Card, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import { QueryState } from "@/components/dashboard/query-state";
import { useCancelSubscription, useSubscription, useUpgradeSubscription } from "@/lib/hooks/use-billing";
import { ApiError } from "@/lib/types/common";
import { formatDateTime, formatNumber } from "@/lib/utils";

// バックエンドのUsageQuotaProperties(daily-openai-calls-free/pro)・GmoPaymentProperties
// (pro-plan-monthly-amount)の既定値と一致させている。プラン一覧を返す専用APIが無いため、
// 料金・上限は表示専用の静的な値として定義する(値を変える場合は両方を更新すること)。
const PLAN_COMPARISON = {
  free: { priceLabel: "無料", dailyAnalysisLimit: 50 },
  pro: { priceLabel: `¥${formatNumber(4980)} / 月`, dailyAnalysisLimit: 500 },
};

const upgradeSchema = z.object({
  cardToken: z.string().min(1, "カードトークンを入力してください"),
});

type UpgradeFormValues = z.infer<typeof upgradeSchema>;

export default function BillingPage() {
  const subscriptionQuery = useSubscription();
  const upgradeMutation = useUpgradeSubscription();
  const cancelMutation = useCancelSubscription();
  const [upgradeError, setUpgradeError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<UpgradeFormValues>({ resolver: zodResolver(upgradeSchema) });

  const onUpgrade = async (values: UpgradeFormValues) => {
    setUpgradeError(null);
    try {
      await upgradeMutation.mutateAsync(values);
      reset();
    } catch (err) {
      setUpgradeError(
        err instanceof ApiError ? err.message : "アップグレードに失敗しました。時間をおいて再度お試しください。",
      );
    }
  };

  const subscription = subscriptionQuery.data;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader title="現在のプラン" description="投稿分析(AI呼び出し)の1日あたりの利用回数はプランにより異なります" />
        <QueryState
          isLoading={subscriptionQuery.isLoading}
          isError={subscriptionQuery.isError}
          error={subscriptionQuery.error}
          onRetry={() => subscriptionQuery.refetch()}
        >
          {subscription && (
            <div className="space-y-2">
              <p className="text-lg font-semibold text-slate-900 dark:text-slate-100">
                {subscription.plan === "PRO" ? "PROプラン" : "FREEプラン"}
                <span className="ml-2 rounded bg-slate-100 px-1.5 py-0.5 text-xs font-normal text-slate-600">
                  {subscription.status}
                </span>
              </p>
              <p className="text-sm text-slate-500">
                1日あたりの投稿分析上限: {subscription.dailyAnalysisLimit}回
              </p>
              {subscription.plan === "PRO" && subscription.currentPeriodEnd && (
                <p className="text-sm text-slate-500">
                  次回更新日: {formatDateTime(subscription.currentPeriodEnd)}
                </p>
              )}
            </div>
          )}
        </QueryState>
      </Card>

      <Card>
        <CardHeader title="プラン比較" description="FREEとPROでできることの違いです" />
        <div className="overflow-x-auto">
          <table className="w-full min-w-[420px] border-collapse text-sm">
            <thead>
              <tr className="border-b border-slate-200 dark:border-slate-700">
                <th scope="col" className="py-2 pr-4 text-left font-medium text-slate-500">
                  項目
                </th>
                <th scope="col" className="py-2 px-4 text-left font-medium text-slate-500">
                  FREE
                </th>
                <th scope="col" className="py-2 pl-4 text-left font-medium text-brand-600">
                  PRO
                </th>
              </tr>
            </thead>
            <tbody>
              <tr className="border-b border-slate-100 dark:border-slate-800">
                <th scope="row" className="py-2 pr-4 text-left font-normal text-slate-600 dark:text-slate-300">
                  料金
                </th>
                <td className="py-2 px-4 text-slate-900 dark:text-slate-100">{PLAN_COMPARISON.free.priceLabel}</td>
                <td className="py-2 pl-4 font-semibold text-slate-900 dark:text-slate-100">
                  {PLAN_COMPARISON.pro.priceLabel}
                </td>
              </tr>
              <tr>
                <th scope="row" className="py-2 pr-4 text-left font-normal text-slate-600 dark:text-slate-300">
                  1日あたりの投稿分析上限
                </th>
                <td className="py-2 px-4 text-slate-900 dark:text-slate-100">
                  {PLAN_COMPARISON.free.dailyAnalysisLimit}回
                </td>
                <td className="py-2 pl-4 font-semibold text-slate-900 dark:text-slate-100">
                  {PLAN_COMPARISON.pro.dailyAnalysisLimit}回
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </Card>

      {subscription?.plan === "FREE" && (
        <Card>
          <CardHeader
            title="PROプランへアップグレード"
            description="決済代行事業者のクライアントサイドJSでトークン化されたカード情報を使用します(生のカード番号は送信されません)"
          />
          <form onSubmit={handleSubmit(onUpgrade)} noValidate className="space-y-4">
            <FormField
              label="カードトークン"
              htmlFor="cardToken"
              error={errors.cardToken?.message}
              // このヒントは決済ゲートウェイが実際にモック実装(常に成功する疑似決済)の場合にのみ
              // 意味を持つ開発者向け情報であり、本番相当の環境(疑似決済ゲートウェイのfail-fastガードが
              // 起動を拒否する状況)でユーザーに表示されてはならない。ビルド時にprocess.env.NODE_ENVで
              // 静的に分岐するため本番ビルドからは完全に除去される。
              hint={process.env.NODE_ENV !== "production" ? "開発/評価環境では任意の文字列で疑似決済が成功します" : undefined}
            >
              <input
                id="cardToken"
                placeholder="tok_xxxxxxxx"
                className={inputClassName(!!errors.cardToken)}
                {...register("cardToken")}
              />
            </FormField>
            {upgradeError && (
              <p role="alert" className="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-600">
                {upgradeError}
              </p>
            )}
            <Button type="submit" isLoading={upgradeMutation.isPending}>
              アップグレードする
            </Button>
          </form>
        </Card>
      )}

      {subscription?.plan === "PRO" && (
        <Card>
          <CardHeader title="解約" description="即時にFREEプランへ戻ります(猶予期間はありません)" />
          <Button variant="danger" onClick={() => cancelMutation.mutate()} isLoading={cancelMutation.isPending}>
            PROプランを解約する
          </Button>
          {cancelMutation.isError && (
            <p className="mt-2 text-sm text-red-500">
              {cancelMutation.error instanceof ApiError ? cancelMutation.error.message : "解約に失敗しました"}
            </p>
          )}
        </Card>
      )}
    </div>
  );
}
