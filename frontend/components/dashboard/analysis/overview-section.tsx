import { Card } from "@/components/ui/card";
import { PlatformBadge } from "@/components/ui/badge";
import { BuzzScoreBadge } from "@/components/dashboard/buzz-score-badge";
import { BuzzScoreRadarChart } from "@/components/charts/buzz-score-radar-chart";
import type { BuzzScoreResult, Post } from "@/lib/types";
import { formatCompactNumber, formatDateTime } from "@/lib/utils";

export function OverviewSection({ post, buzzScore }: { post: Post; buzzScore: BuzzScoreResult }) {
  return (
    <Card>
      <div className="flex flex-col gap-6 md:flex-row">
        <div className="flex gap-4 md:w-1/2">
          <div className="h-24 w-24 shrink-0 overflow-hidden rounded-xl bg-slate-100">
            {post.thumbnailUrl ? (
              // eslint-disable-next-line @next/next/no-img-element
              <img src={post.thumbnailUrl} alt="" className="h-full w-full object-cover" />
            ) : (
              <div className="flex h-full w-full items-center justify-center text-3xl text-slate-300">
                🖼️
              </div>
            )}
          </div>
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <PlatformBadge platform={post.platform} />
              <span className="text-sm text-slate-500">
                @{post.accountHandle ?? post.authorName ?? "unknown"}
              </span>
            </div>
            <p className="mt-1 line-clamp-3 text-sm text-slate-700">{post.caption}</p>
            <div className="mt-2 flex flex-wrap gap-x-3 gap-y-1 text-xs text-slate-400">
              <span>❤️ {formatCompactNumber(post.likeCount)}</span>
              <span>💬 {formatCompactNumber(post.commentCount)}</span>
              <span>🔁 {formatCompactNumber(post.shareCount)}</span>
              {post.viewCount !== undefined && <span>👁️ {formatCompactNumber(post.viewCount)}</span>}
              <span>{formatDateTime(post.postedAt ?? post.publishedAt)}</span>
            </div>
          </div>
        </div>

        <div className="flex flex-1 items-center gap-6 border-t border-slate-100 pt-4 md:border-l md:border-t-0 md:pl-6 md:pt-0">
          <BuzzScoreBadge score={buzzScore.totalScore} size="lg" />
          <div className="flex-1">
            <BuzzScoreRadarChart breakdown={buzzScore.breakdown} />
          </div>
        </div>
      </div>
    </Card>
  );
}
