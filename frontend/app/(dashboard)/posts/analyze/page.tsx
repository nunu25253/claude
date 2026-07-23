"use client";

import { Card, CardHeader } from "@/components/ui/card";
import { ErrorState } from "@/components/ui/error-state";
import { AnalyzeUrlForm } from "@/components/dashboard/analysis/analyze-url-form";
import { AnalyzeProgress } from "@/components/dashboard/analysis/analyze-progress";
import { AnalysisResult } from "@/components/dashboard/analysis/analysis-result";
import { useAnalyzePost } from "@/lib/hooks/use-posts";

export default function AnalyzePostPage() {
  const analyzeMutation = useAnalyzePost();

  const handleSubmit = (url: string) => {
    analyzeMutation.mutate({ postUrl: url });
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader
          title="投稿URLを分析"
          description="Instagram / TikTok / X の公開投稿URLを入力すると、AIがバズの理由と改善案を提案します。"
        />
        <AnalyzeUrlForm onSubmit={handleSubmit} isSubmitting={analyzeMutation.isPending} />
      </Card>

      {analyzeMutation.isPending && <AnalyzeProgress />}

      {analyzeMutation.isError && (
        <ErrorState error={analyzeMutation.error} onRetry={() => analyzeMutation.reset()} />
      )}

      {analyzeMutation.isSuccess && analyzeMutation.data && (
        <AnalysisResult result={analyzeMutation.data} />
      )}
    </div>
  );
}
