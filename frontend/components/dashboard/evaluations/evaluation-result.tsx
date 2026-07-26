import { Card, CardHeader } from "@/components/ui/card";
import type { ContentEvaluation } from "@/lib/types";

function scoreColorClass(score: number): string {
  if (score >= 70) return "text-emerald-600";
  if (score >= 40) return "text-amber-600";
  return "text-red-500";
}

export function EvaluationResult({ result }: { result: ContentEvaluation }) {
  return (
    <Card>
      <CardHeader title="評価結果" description={result.title} />

      <div className="flex flex-wrap gap-6">
        <div>
          <p className="text-xs font-medium text-slate-500">予測投稿スコア</p>
          <p className={`text-3xl font-bold ${scoreColorClass(result.predictedScore)}`}>
            {result.predictedScore}
            <span className="text-base font-normal text-slate-500 dark:text-slate-400">/100</span>
          </p>
        </div>
        <div>
          <p className="text-xs font-medium text-slate-500">元企画との一致率</p>
          <p className="text-3xl font-bold text-slate-800">
            {result.matchRatePercent == null ? (
              <span className="text-base font-normal text-slate-500 dark:text-slate-400">企画ID未指定</span>
            ) : (
              <>
                {result.matchRatePercent.toFixed(0)}
                <span className="text-base font-normal text-slate-500 dark:text-slate-400">%</span>
              </>
            )}
          </p>
        </div>
      </div>

      <dl className="mt-4 space-y-3 text-sm">
        {result.targetAudienceEstimate && (
          <div>
            <dt className="font-medium text-slate-500">想定ターゲット</dt>
            <dd className="text-slate-700">{result.targetAudienceEstimate}</dd>
          </div>
        )}
        {result.hookImprovement && (
          <div>
            <dt className="font-medium text-slate-500">フック改善案</dt>
            <dd className="text-slate-700">{result.hookImprovement}</dd>
          </div>
        )}
        {result.ctaImprovement && (
          <div>
            <dt className="font-medium text-slate-500">CTA改善案</dt>
            <dd className="text-slate-700">{result.ctaImprovement}</dd>
          </div>
        )}
        {result.improvementSuggestions.length > 0 && (
          <div>
            <dt className="font-medium text-slate-500">改善提案</dt>
            <dd>
              <ul className="mt-1 list-disc space-y-1 pl-5 text-slate-700">
                {result.improvementSuggestions.map((s, i) => (
                  <li key={i}>{s}</li>
                ))}
              </ul>
            </dd>
          </div>
        )}
      </dl>
    </Card>
  );
}
