"use client";

import { Card, CardHeader } from "@/components/ui/card";
import { ErrorState } from "@/components/ui/error-state";
import { EvaluationForm } from "@/components/dashboard/evaluations/evaluation-form";
import { EvaluationResult } from "@/components/dashboard/evaluations/evaluation-result";
import { useEvaluatePost } from "@/lib/hooks/use-evaluations";

export default function EvaluationsPage() {
  const evaluateMutation = useEvaluatePost();

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader
          title="投稿評価"
          description="作成した台本・カルーセル等の投稿内容をAIが評価します。企画IDを指定すると元企画との一致率も算出します。"
        />
        <EvaluationForm
          onSubmit={(payload) => evaluateMutation.mutate(payload)}
          isSubmitting={evaluateMutation.isPending}
        />
      </Card>

      {evaluateMutation.isError && (
        <ErrorState error={evaluateMutation.error} onRetry={() => evaluateMutation.reset()} />
      )}

      {evaluateMutation.isSuccess && evaluateMutation.data && (
        <EvaluationResult result={evaluateMutation.data} />
      )}
    </div>
  );
}
