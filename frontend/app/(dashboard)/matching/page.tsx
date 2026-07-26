"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Card, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { FormField, inputClassName } from "@/components/ui/form-field";
import { Select } from "@/components/ui/select";
import { PostCard } from "@/components/dashboard/post-card";
import { ErrorState } from "@/components/ui/error-state";
import { useEvaluateMatching } from "@/lib/hooks/use-matching";
import { GENRE_OPTIONS, PLATFORM_OPTIONS } from "@/lib/constants";
import type { MatchingConditionRequest } from "@/lib/types";

const conditionSchema = z.object({
  productName: z.string().optional(),
  brand: z.string().optional(),
  keyword: z.string().optional(),
  genre: z.string().optional(),
  platform: z.string().optional(),
  targetAgeRange: z.string().optional(),
  targetGender: z.string().optional(),
  purpose: z.string().optional(),
});

type ConditionFormValues = z.infer<typeof conditionSchema>;

/**
 * 案件(商品・ブランド)の条件と、分析済み投稿群の一致率を算出する画面
 * (AIマーケティングOS Phase6: ユーザー条件分析)。バックエンドには実装済みだったが
 * フロントエンドから未接続だった機能を、インフルエンサーマーケティング案件の
 * 「どの投稿者/投稿がこの案件に合うか」を判定するツールとして機能化する。
 */
export default function MatchingPage() {
  const evaluateMutation = useEvaluateMatching();

  const { register, handleSubmit } = useForm<ConditionFormValues>({
    resolver: zodResolver(conditionSchema),
  });

  const onSubmit = (values: ConditionFormValues) => {
    const payload: MatchingConditionRequest = {};
    if (values.productName) payload.productName = values.productName;
    if (values.brand) payload.brand = values.brand;
    if (values.keyword) payload.keyword = values.keyword;
    if (values.genre) payload.genre = values.genre;
    if (values.platform) payload.platform = values.platform as MatchingConditionRequest["platform"];
    if (values.targetAgeRange) payload.targetAgeRange = values.targetAgeRange;
    if (values.targetGender) payload.targetGender = values.targetGender;
    if (values.purpose) payload.purpose = values.purpose;
    evaluateMutation.mutate(payload);
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader
          title="案件マッチ度チェック"
          description="商品・ブランドの条件を入力すると、分析済み投稿の中から条件に合う投稿をAIが一致率順に並べます"
        />
        <form onSubmit={handleSubmit(onSubmit)} noValidate className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <FormField label="商品名" htmlFor="productName">
            <input id="productName" className={inputClassName(false)} placeholder="例: 保湿クリーム" {...register("productName")} />
          </FormField>
          <FormField label="ブランド名" htmlFor="brand">
            <input id="brand" className={inputClassName(false)} placeholder="例: ○○コスメ" {...register("brand")} />
          </FormField>
          <FormField label="キーワード" htmlFor="keyword">
            <input id="keyword" className={inputClassName(false)} placeholder="自由文で条件を入力" {...register("keyword")} />
          </FormField>
          <FormField label="ジャンル" htmlFor="genre">
            <Select id="genre" options={GENRE_OPTIONS} placeholder="指定なし" {...register("genre")} />
          </FormField>
          <FormField label="SNS" htmlFor="platform">
            <Select id="platform" options={PLATFORM_OPTIONS} placeholder="指定なし" {...register("platform")} />
          </FormField>
          <FormField label="ターゲット年齢層" htmlFor="targetAgeRange">
            <input id="targetAgeRange" className={inputClassName(false)} placeholder="例: 20代" {...register("targetAgeRange")} />
          </FormField>
          <FormField label="ターゲット性別" htmlFor="targetGender">
            <input id="targetGender" className={inputClassName(false)} placeholder="例: 女性" {...register("targetGender")} />
          </FormField>
          <FormField label="投稿目的" htmlFor="purpose">
            <input id="purpose" className={inputClassName(false)} placeholder="例: 認知獲得" {...register("purpose")} />
          </FormField>
          <div className="sm:col-span-2">
            <Button type="submit" isLoading={evaluateMutation.isPending}>
              一致率を算出する
            </Button>
          </div>
        </form>
      </Card>

      {evaluateMutation.isError && (
        <ErrorState error={evaluateMutation.error} onRetry={() => evaluateMutation.reset()} />
      )}

      {evaluateMutation.isSuccess && (
        <Card>
          <CardHeader title="一致率の高い投稿" description={`${evaluateMutation.data.length}件`} />
          {evaluateMutation.data.length === 0 ? (
            <p className="text-sm text-slate-500">条件に一致する投稿が見つかりませんでした。条件を減らして再度お試しください。</p>
          ) : (
            <div className="space-y-2">
              {evaluateMutation.data.map((result) => (
                <PostCard
                  key={result.post.id}
                  post={result.post}
                  footer={
                    <p className="mt-1.5 text-xs font-medium text-brand-600 dark:text-brand-400">
                      一致率 {result.matchRatePercent.toFixed(0)}%
                    </p>
                  }
                />
              ))}
            </div>
          )}
        </Card>
      )}
    </div>
  );
}
