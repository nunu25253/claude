import { Card, CardHeader } from "@/components/ui/card";
import type { SentimentAnalysis } from "@/lib/types";
import { cn, formatPercent } from "@/lib/utils";

const SENTIMENT_LABEL: Record<SentimentAnalysis["overallSentiment"], string> = {
  POSITIVE: "ポジティブ",
  NEUTRAL: "ニュートラル",
  NEGATIVE: "ネガティブ",
  MIXED: "混合",
};

export function SentimentSection({ sentiment }: { sentiment: SentimentAnalysis }) {
  const bars = [
    { label: "ポジティブ", value: sentiment.positiveRatio, className: "bg-emerald-500" },
    { label: "ニュートラル", value: sentiment.neutralRatio, className: "bg-slate-400" },
    { label: "ネガティブ", value: sentiment.negativeRatio, className: "bg-red-500" },
  ];

  return (
    <Card>
      <CardHeader
        title="😊 感情分析"
        description={`総合評価: ${SENTIMENT_LABEL[sentiment.overallSentiment]}`}
      />
      <div className="space-y-2">
        {bars.map((bar) => (
          <div key={bar.label}>
            <div className="mb-0.5 flex justify-between text-xs text-slate-500">
              <span>{bar.label}</span>
              <span>{formatPercent(bar.value)}</span>
            </div>
            <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100">
              <div
                className={cn("h-full rounded-full", bar.className)}
                style={{ width: `${Math.min(100, bar.value * 100)}%` }}
              />
            </div>
          </div>
        ))}
      </div>
      {sentiment.emotionTags.length > 0 && (
        <div className="mt-4 flex flex-wrap gap-1.5">
          {sentiment.emotionTags.map((tag) => (
            <span key={tag} className="badge bg-brand-50 text-brand-700">
              {tag}
            </span>
          ))}
        </div>
      )}
    </Card>
  );
}
