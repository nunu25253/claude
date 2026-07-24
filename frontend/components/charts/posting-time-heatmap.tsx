import { cn } from "@/lib/utils";
import type { PostingTimeDistributionItem } from "@/lib/types";

const DAY_LABELS = ["日", "月", "火", "水", "木", "金", "土"];
const HOUR_BUCKETS = [0, 3, 6, 9, 12, 15, 18, 21]; // 3時間単位でまとめて見やすくする

/** 曜日 x 時間帯の投稿頻度をヒートマップ表示する（recharts非対応のため独自グリッド実装） */
export function PostingTimeHeatmap({ data }: { data: PostingTimeDistributionItem[] }) {
  const maxCount = Math.max(1, ...data.map((d) => d.postCount));

  function countFor(day: number, bucketStart: number): number {
    return data
      .filter((d) => d.dayOfWeek === day && d.hour >= bucketStart && d.hour < bucketStart + 3)
      .reduce((sum, d) => sum + d.postCount, 0);
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[480px] border-separate border-spacing-1 text-center text-xs">
        <thead>
          <tr>
            <th className="w-10" />
            {HOUR_BUCKETS.map((h) => (
              <th key={h} className="font-normal text-slate-500 dark:text-slate-400">
                {h}時
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {DAY_LABELS.map((label, day) => (
            <tr key={label}>
              <th className="font-medium text-slate-500">{label}</th>
              {HOUR_BUCKETS.map((bucketStart) => {
                const count = countFor(day, bucketStart);
                const intensity = count / maxCount;
                return (
                  <td key={bucketStart}>
                    <div
                      className={cn("h-7 w-full rounded-md", count === 0 && "bg-slate-100")}
                      style={
                        count > 0
                          ? { backgroundColor: `rgba(74, 99, 245, ${0.15 + intensity * 0.75})` }
                          : undefined
                      }
                      title={`${label}曜 ${bucketStart}時台: ${count}件`}
                    />
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
