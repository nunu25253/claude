"use client";

import { Card, CardHeader } from "@/components/ui/card";
import { ErrorState } from "@/components/ui/error-state";
import { LoadingState } from "@/components/ui/loading-state";
import { EmptyState } from "@/components/ui/empty-state";
import { ProposalGenerationForm } from "@/components/dashboard/proposals/proposal-generation-form";
import { ProposalCard } from "@/components/dashboard/proposals/proposal-card";
import { AnalyzedPostsList } from "@/components/dashboard/proposals/analyzed-posts-list";
import { useGenerateProposals } from "@/lib/hooks/use-proposals";

export default function ProposalsPage() {
  const generateMutation = useGenerateProposals();

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader
          title="AI企画生成"
          description="共通点分析の対象とする投稿IDを指定すると、AIが投稿企画（既定20件）を生成します。各企画から台本・カルーセルもその場で生成できます。"
        />
        <ProposalGenerationForm
          onSubmit={(payload) => generateMutation.mutate(payload)}
          isSubmitting={generateMutation.isPending}
        />
      </Card>

      {generateMutation.isPending && (
        <Card>
          <LoadingState label="AIが企画を生成しています... 数十秒かかる場合があります" />
        </Card>
      )}

      {generateMutation.isError && (
        <ErrorState error={generateMutation.error} onRetry={() => generateMutation.reset()} />
      )}

      {generateMutation.isSuccess &&
        (generateMutation.data.length === 0 ? (
          <EmptyState title="企画が生成されませんでした" />
        ) : (
          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            {generateMutation.data.map((proposal) => (
              <ProposalCard key={proposal.id} proposal={proposal} />
            ))}
          </div>
        ))}

      <AnalyzedPostsList />
    </div>
  );
}
