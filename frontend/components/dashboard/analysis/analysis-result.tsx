"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import type { AnalyzePostResponse } from "@/lib/types";
import { useCreateSavedAnalysis } from "@/lib/hooks/use-saved-analyses";
import { Button } from "@/components/ui/button";
import { Card, CardHeader } from "@/components/ui/card";
import { OverviewSection } from "./overview-section";
import { InsightListCard } from "./insight-list-card";
import { SimilarPostsSection } from "./similar-posts-section";
import { BuzzScoreHistorySection } from "./buzz-score-history-section";
import { ScoreComparisonSection } from "./score-comparison-section";

export function AnalysisResult({
  result,
  hideSaveAction = false,
}: {
  result: AnalyzePostResponse;
  /** 保存済み一覧など、すでに保存済みであることが自明な文脈では保存ボタンを非表示にする */
  hideSaveAction?: boolean;
}) {
  const { post, analysis, buzzScore, similarPosts } = result;
  const [saved, setSaved] = useState(false);
  const saveMutation = useCreateSavedAnalysis();
  const router = useRouter();

  const handleSave = async () => {
    try {
      await saveMutation.mutateAsync({ postId: post.id });
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
      <OverviewSection post={post} buzzScore={buzzScore} />

      {/* 前回投稿比・同ジャンル平均比(比較対象があれば表示) */}
      <ScoreComparisonSection postId={post.id} currentScore={buzzScore.totalScore} />

      {/* BuzzScore推移(2回目以降の再分析があれば表示) */}
      <BuzzScoreHistorySection postId={post.id} />

      {analysis.genre && (
        <Card>
          <CardHeader title="🏷️ ジャンル" description={analysis.subGenre} />
          <p className="text-sm text-slate-700">{analysis.genre}</p>
        </Card>
      )}

      {/* 伸びた理由 / ターゲット層 / フック / CTA */}
      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        <InsightListCard title="伸びた理由" icon="💡" text={analysis.whyItWentViral} />
        <InsightListCard title="ターゲット層" icon="🎯" text={analysis.targetAudience} />
        <InsightListCard title="冒頭フック分析" icon="🪝" text={analysis.hook} />
        <InsightListCard title="CTA分析" icon="📣" text={analysis.callToAction} />
      </div>

      <InsightListCard title="感情分析" icon="😊" text={analysis.sentimentAnalysis} />

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        <InsightListCard title="タイトル分析" icon="📝" text={analysis.titleAnalysis} />
        <InsightListCard title="文章分析" icon="✍️" text={analysis.textAnalysis} />
      </div>

      {analysis.videoStructureAnalysis && (
        <InsightListCard title="動画構成分析" icon="🎬" text={analysis.videoStructureAnalysis} />
      )}
      {analysis.carouselStructureAnalysis && (
        <InsightListCard title="カルーセル構成分析" icon="🖼️" text={analysis.carouselStructureAnalysis} />
      )}

      <InsightListCard title="投稿時間分析" icon="⏰" text={analysis.postingTimeAnalysis} />

      <InsightListCard title="ハッシュタグ分析" icon="🏷️" text={analysis.hashtagAnalysis} />

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
        <InsightListCard title="強み" icon="✅" text={analysis.strengths} />
        <InsightListCard title="弱み" icon="⚠️" text={analysis.weaknesses} />
      </div>

      <InsightListCard title="改善案" icon="🛠️" text={analysis.improvementSuggestions} />

      <SimilarPostsSection posts={similarPosts} />
    </div>
  );
}
