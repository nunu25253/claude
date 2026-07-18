"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { AnalyzePostResponse } from "@/lib/types";
import { useCreateSavedAnalysis } from "@/lib/hooks/use-saved-analyses";
import { Button } from "@/components/ui/button";
import { OverviewSection } from "./overview-section";
import { InsightListCard } from "./insight-list-card";
import { SentimentSection } from "./sentiment-section";
import { VideoStructureSection } from "./video-structure-section";
import { CarouselStructureSection } from "./carousel-structure-section";
import { TitleCaptionSection } from "./title-caption-section";
import { PostingTimeSection } from "./posting-time-section";
import { HashtagAnalysisSection } from "./hashtag-analysis-section";
import { SimilarPostsSection } from "./similar-posts-section";

export function AnalysisResult({
  result,
  hideSaveAction = false,
}: {
  result: AnalyzePostResponse;
  /** 保存済み一覧など、すでに保存済みであることが自明な文脈では保存ボタンを非表示にする */
  hideSaveAction?: boolean;
}) {
  const { post, analysis } = result;
  const [saved, setSaved] = useState(false);
  const saveMutation = useCreateSavedAnalysis();
  const router = useRouter();

  const handleSave = async () => {
    try {
      await saveMutation.mutateAsync({ postId: post.id, analysisId: analysis.id });
      setSaved(true);
    } catch {
      // エラーは saveMutation.isError 経由でボタン付近に表示する
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-slate-900">AI分析結果</h2>
        {!hideSaveAction && (
          <div className="flex items-center gap-2">
            {saveMutation.isError && (
              <span className="text-xs text-red-500">保存に失敗しました</span>
            )}
            <Button
              variant={saved ? "secondary" : "primary"}
              onClick={saved ? () => router.push("/saved") : handleSave}
              isLoading={saveMutation.isPending}
            >
              {saved ? "保存済み一覧を見る" : "🔖 この分析を保存"}
            </Button>
          </div>
        )}
      </div>

      {/* 概要 + BuzzScore */}
      <OverviewSection post={post} analysis={analysis} />

      {/* 伸びた理由 / ターゲット層 / フック / CTA */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        <InsightListCard title="伸びた理由" icon="💡" items={analysis.viralReasons} />
        <InsightListCard title="ターゲット層" icon="🎯" items={analysis.targetAudience} />
        <InsightListCard title="冒頭フック分析" icon="🪝" items={analysis.hooks} />
        <InsightListCard title="CTA分析" icon="📣" items={analysis.callToActions} />
      </div>

      <SentimentSection sentiment={analysis.sentiment} />

      {analysis.videoStructure && <VideoStructureSection segments={analysis.videoStructure} />}
      {analysis.carouselStructure && (
        <CarouselStructureSection slides={analysis.carouselStructure} />
      )}

      <TitleCaptionSection
        titleAnalysis={analysis.titleAnalysis}
        captionAnalysis={analysis.captionAnalysis}
      />

      <PostingTimeSection postingTimeAnalysis={analysis.postingTimeAnalysis} />

      <HashtagAnalysisSection items={analysis.hashtagAnalysis} />

      <InsightListCard title="改善案" icon="🛠️" items={analysis.improvementSuggestions} />

      <SimilarPostsSection posts={analysis.similarPosts} />
    </div>
  );
}
