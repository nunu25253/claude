import { Card, CardHeader } from "@/components/ui/card";
import type { VideoStructureSegment } from "@/lib/types";

export function VideoStructureSection({ segments }: { segments: VideoStructureSegment[] }) {
  if (segments.length === 0) return null;

  return (
    <Card>
      <CardHeader title="🎬 動画構成分析" description="タイムライン別の構成要素" />
      <ol className="space-y-3">
        {segments
          .slice()
          .sort((a, b) => a.order - b.order)
          .map((seg) => (
            <li key={seg.order} className="flex gap-3">
              <div className="flex w-20 shrink-0 flex-col items-center">
                <span className="flex h-7 w-7 items-center justify-center rounded-full bg-brand-100 text-xs font-bold text-brand-700">
                  {seg.order}
                </span>
                <span className="mt-1 text-center text-[11px] text-slate-400">
                  {seg.startSeconds}s-{seg.endSeconds}s
                </span>
              </div>
              <div className="flex-1 rounded-lg border border-slate-100 px-3 py-2">
                <p className="text-sm font-semibold text-slate-800">{seg.label}</p>
                <p className="mt-0.5 text-sm text-slate-500">{seg.description}</p>
              </div>
            </li>
          ))}
      </ol>
    </Card>
  );
}
