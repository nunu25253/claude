import { PlatformBadge } from "@/components/ui/badge";
import { BuzzScoreBadge } from "./buzz-score-badge";
import type { Post } from "@/lib/types";
import { formatCompactNumber, formatDateTime } from "@/lib/utils";

interface PostCardProps {
  post: Post;
  rank?: number;
  footer?: React.ReactNode;
}

export function PostCard({ post, rank, footer }: PostCardProps) {
  return (
    <div className="flex gap-3 rounded-xl border border-slate-100 p-3 transition hover:border-brand-200 hover:bg-brand-50/30">
      {rank !== undefined && (
        <div className="flex w-7 shrink-0 items-center justify-center text-sm font-bold text-slate-400">
          {rank}
        </div>
      )}

      <div className="h-16 w-16 shrink-0 overflow-hidden rounded-lg bg-slate-100">
        {post.thumbnailUrl ? (
          // eslint-disable-next-line @next/next/no-img-element -- 外部SNSサムネイルのため next/image のドメイン許可設定を都度増やさず img で表示
          <img
            src={post.thumbnailUrl}
            alt=""
            className="h-full w-full object-cover"
            loading="lazy"
          />
        ) : (
          <div className="flex h-full w-full items-center justify-center text-xl text-slate-300">
            🖼️
          </div>
        )}
      </div>

      <div className="min-w-0 flex-1">
        <div className="flex flex-wrap items-center gap-1.5">
          <PlatformBadge platform={post.platform} />
          <span className="truncate text-xs text-slate-400">@{post.accountHandle}</span>
        </div>
        <p className="mt-1 line-clamp-2 text-sm text-slate-700">{post.caption || "(キャプションなし)"}</p>
        <div className="mt-1.5 flex flex-wrap gap-x-3 gap-y-0.5 text-xs text-slate-400">
          <span>❤️ {formatCompactNumber(post.likeCount)}</span>
          <span>💬 {formatCompactNumber(post.commentCount)}</span>
          <span>🔁 {formatCompactNumber(post.shareCount)}</span>
          <span>{formatDateTime(post.postedAt)}</span>
        </div>
        {footer}
      </div>

      {post.buzzScore !== undefined && <BuzzScoreBadge score={post.buzzScore} size="sm" />}
    </div>
  );
}
