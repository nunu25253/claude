"use client";

import { useScoreComparison } from "@/lib/hooks/use-posts";
import { cn } from "@/lib/utils";

/**
 * 分析結果に「前回投稿比」「同ジャンル平均比」の差分バッジを表示する。
 * 分析結果が履歴化されているのに画面上は今回の1件しか見えず、良し悪しの相対感覚を
 * 持てないという指摘への対応(戦略監査レポート4章、優先度: 最高)。
 */
export function ScoreComparisonSection({ postId, currentScore }: { postId: string; currentScore: number }) {
  const comparisonQuery = useScoreComparison(postId);
  const comparison = comparisonQuery.data;

  if (comparisonQuery.isLoading || comparisonQuery.isError || !comparison) {
    return null;
  }
  if (comparison.previousPostScore == null && comparison.genreAverageScore == null) {
    return null;
  }

  return (
    <div className="flex flex-wrap gap-2">
      {comparison.previousPostScore != null && (
        <DeltaBadge
          label="前回投稿比"
          currentScore={currentScore}
          baselineScore={comparison.previousPostScore}
        />
      )}
      {comparison.genreAverageScore != null && (
        <DeltaBadge
          label={`同ジャンル平均比(${comparison.genreSampleSize}件)`}
          currentScore={currentScore}
          baselineScore={comparison.genreAverageScore}
        />
      )}
    </div>
  );
}

function DeltaBadge({
  label,
  currentScore,
  baselineScore,
}: {
  label: string;
  currentScore: number;
  baselineScore: number;
}) {
  const delta = currentScore - baselineScore;
  const isFlat = Math.abs(delta) < 0.05;
  const isUp = delta > 0;

  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-medium",
        isFlat
          ? "border-slate-200 bg-slate-50 text-slate-600 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300"
          : isUp
            ? "border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-800 dark:bg-emerald-950 dark:text-emerald-400"
            : "border-red-200 bg-red-50 text-red-700 dark:border-red-800 dark:bg-red-950 dark:text-red-400",
      )}
    >
      <span className="text-slate-500 dark:text-slate-400">{label}</span>
      <span aria-hidden>{isFlat ? "→" : isUp ? "↑" : "↓"}</span>
      <span>
        {isFlat ? "±0" : `${isUp ? "+" : ""}${delta.toFixed(1)}`}
      </span>
    </span>
  );
}
