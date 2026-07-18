"use client";

import { useState } from "react";
import { Card } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ErrorState } from "@/components/ui/error-state";
import { useGenerateCarousel, useGenerateScript } from "@/lib/hooks/use-proposals";
import type { ContentProposal } from "@/lib/types";

const DURATIONS: Array<30 | 60 | 90> = [30, 60, 90];

export function ProposalCard({ proposal }: { proposal: ContentProposal }) {
  const [showScript, setShowScript] = useState(false);
  const [showCarousel, setShowCarousel] = useState(false);
  const scriptMutation = useGenerateScript();
  const carouselMutation = useGenerateCarousel();

  return (
    <Card>
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <p className="text-xs font-medium text-slate-400">#{proposal.sequenceNumber}</p>
          <h3 className="mt-0.5 truncate text-base font-semibold text-slate-900">{proposal.title}</h3>
        </div>
        {proposal.recommendedFormat && <Badge>{proposal.recommendedFormat}</Badge>}
      </div>

      <dl className="mt-3 space-y-2 text-sm">
        {proposal.hookPattern && (
          <div>
            <dt className="font-medium text-slate-500">フック</dt>
            <dd className="text-slate-700">{proposal.hookPattern}</dd>
          </div>
        )}
        {proposal.structureSummary && (
          <div>
            <dt className="font-medium text-slate-500">構成</dt>
            <dd className="text-slate-700">{proposal.structureSummary}</dd>
          </div>
        )}
        {proposal.callToAction && (
          <div>
            <dt className="font-medium text-slate-500">CTA</dt>
            <dd className="text-slate-700">{proposal.callToAction}</dd>
          </div>
        )}
        {proposal.targetAudience && (
          <div>
            <dt className="font-medium text-slate-500">ターゲット</dt>
            <dd className="text-slate-700">{proposal.targetAudience}</dd>
          </div>
        )}
        {proposal.reasoning && (
          <div>
            <dt className="font-medium text-slate-500">根拠</dt>
            <dd className="text-slate-500">{proposal.reasoning}</dd>
          </div>
        )}
      </dl>

      <div className="mt-4 flex flex-wrap gap-2 border-t border-slate-100 pt-3">
        {DURATIONS.map((duration) => (
          <Button
            key={duration}
            type="button"
            variant="secondary"
            isLoading={scriptMutation.isPending && scriptMutation.variables?.durationSeconds === duration}
            onClick={() => {
              setShowScript(true);
              scriptMutation.mutate({ proposalId: proposal.id, durationSeconds: duration });
            }}
          >
            台本生成 {duration}秒
          </Button>
        ))}
        <Button
          type="button"
          variant="secondary"
          isLoading={carouselMutation.isPending}
          onClick={() => {
            setShowCarousel(true);
            carouselMutation.mutate({ proposalId: proposal.id });
          }}
        >
          カルーセル生成
        </Button>
      </div>

      {showScript && scriptMutation.isError && (
        <div className="mt-3">
          <ErrorState error={scriptMutation.error} onRetry={() => scriptMutation.reset()} />
        </div>
      )}
      {showScript && scriptMutation.isSuccess && scriptMutation.data && (
        <div className="mt-3 rounded-lg border border-slate-100 bg-slate-50 p-3 text-sm">
          <p className="font-medium text-slate-700">
            台本（{scriptMutation.data.durationSeconds}秒 / BGM: {scriptMutation.data.bgmImage ?? "-"}）
          </p>
          <ol className="mt-2 space-y-2">
            {scriptMutation.data.cuts.map((cut) => (
              <li key={cut.cutNumber} className="rounded border border-slate-200 bg-white p-2">
                <p className="text-xs font-semibold text-slate-500">
                  カット{cut.cutNumber}（{cut.startSecond}〜{cut.endSecond}秒）
                </p>
                {cut.narration && <p className="mt-1 text-slate-700">ナレーション: {cut.narration}</p>}
                {cut.telop && <p className="text-slate-700">テロップ: {cut.telop}</p>}
                {cut.visualDirection && <p className="text-slate-500">映像指示: {cut.visualDirection}</p>}
              </li>
            ))}
          </ol>
        </div>
      )}

      {showCarousel && carouselMutation.isError && (
        <div className="mt-3">
          <ErrorState error={carouselMutation.error} onRetry={() => carouselMutation.reset()} />
        </div>
      )}
      {showCarousel && carouselMutation.isSuccess && carouselMutation.data && (
        <div className="mt-3 rounded-lg border border-slate-100 bg-slate-50 p-3 text-sm">
          <p className="font-medium text-slate-700">カルーセル（{carouselMutation.data.pages.length}ページ）</p>
          <ol className="mt-2 space-y-2">
            {carouselMutation.data.pages.map((page) => (
              <li key={page.pageNumber} className="rounded border border-slate-200 bg-white p-2">
                <p className="text-xs font-semibold text-slate-500">
                  {page.pageNumber}ページ目（{page.role}）
                </p>
                <p className="mt-1 text-slate-700">{page.headline}</p>
                {page.bodyText && <p className="text-slate-500">{page.bodyText}</p>}
              </li>
            ))}
          </ol>
        </div>
      )}
    </Card>
  );
}
