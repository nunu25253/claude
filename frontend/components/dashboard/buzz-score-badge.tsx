import { cn } from "@/lib/utils";

/** BuzzScore(0-100)を丸バッジで表示する。スコア帯によって色を変える */
export function BuzzScoreBadge({ score, size = "md" }: { score: number; size?: "sm" | "md" | "lg" }) {
  const tier =
    score >= 80
      ? { label: "TOP", className: "bg-purple-100 text-purple-700 border-purple-200" }
      : score >= 60
        ? { label: "HIGH", className: "bg-red-100 text-red-700 border-red-200" }
        : score >= 40
          ? { label: "MID", className: "bg-amber-100 text-amber-700 border-amber-200" }
          : { label: "LOW", className: "bg-slate-100 text-slate-600 border-slate-200" };

  const sizeClass =
    size === "lg" ? "h-16 w-16 text-lg" : size === "sm" ? "h-9 w-9 text-xs" : "h-12 w-12 text-sm";

  return (
    <div className="flex flex-col items-center gap-1">
      <div
        className={cn(
          "flex items-center justify-center rounded-full border-2 font-bold",
          sizeClass,
          tier.className,
        )}
        title={`BuzzScore: ${score}`}
      >
        {Math.round(score)}
      </div>
      <span className="text-[10px] font-semibold uppercase tracking-wide text-slate-400">
        {tier.label}
      </span>
    </div>
  );
}
