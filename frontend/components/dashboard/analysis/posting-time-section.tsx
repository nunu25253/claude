import { Card, CardHeader } from "@/components/ui/card";
import type { PostAnalysis } from "@/lib/types";
import { formatDateTime } from "@/lib/utils";

export function PostingTimeSection({
  postingTimeAnalysis,
}: {
  postingTimeAnalysis: PostAnalysis["postingTimeAnalysis"];
}) {
  return (
    <Card>
      <CardHeader title="⏰ 投稿時間分析" />
      <div className="flex flex-wrap items-center gap-3">
        <span className="text-sm text-slate-500">
          投稿日時: {formatDateTime(postingTimeAnalysis.postedAt)}
        </span>
        <span
          className={
            postingTimeAnalysis.isOptimalTiming
              ? "badge bg-emerald-50 text-emerald-700"
              : "badge bg-amber-50 text-amber-700"
          }
        >
          {postingTimeAnalysis.isOptimalTiming ? "最適な時間帯" : "改善余地あり"}
        </span>
      </div>
      <p className="mt-3 text-sm text-slate-600">{postingTimeAnalysis.comment}</p>
      {postingTimeAnalysis.recommendedTimeWindows.length > 0 && (
        <div className="mt-3">
          <p className="mb-1.5 text-xs font-semibold text-slate-500">おすすめ投稿時間帯</p>
          <div className="flex flex-wrap gap-1.5">
            {postingTimeAnalysis.recommendedTimeWindows.map((w) => (
              <span key={w} className="badge bg-brand-50 text-brand-700">
                {w}
              </span>
            ))}
          </div>
        </div>
      )}
    </Card>
  );
}
