import { cn } from "@/lib/utils";
import type { Platform } from "@/lib/types";

export function Badge({
  className,
  children,
}: {
  className?: string;
  children: React.ReactNode;
}) {
  return (
    <span className={cn("badge bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-300", className)}>
      {children}
    </span>
  );
}

const PLATFORM_STYLE: Record<Platform, { label: string; className: string }> = {
  INSTAGRAM: { label: "Instagram", className: "bg-pink-50 text-pink-600" },
  TIKTOK: { label: "TikTok", className: "bg-slate-900 text-white" },
  X: { label: "X", className: "bg-slate-100 text-slate-800" },
};

export function PlatformBadge({ platform }: { platform: Platform }) {
  const style = PLATFORM_STYLE[platform] ?? {
    label: platform,
    className: "bg-slate-100 text-slate-600",
  };
  return <span className={cn("badge", style.className)}>{style.label}</span>;
}
