import { Card } from "@/components/ui/card";
import { cn } from "@/lib/utils";

interface KpiCardProps {
  label: string;
  value: string;
  icon?: string;
  trend?: { direction: "up" | "down"; label: string };
}

export function KpiCard({ label, value, icon, trend }: KpiCardProps) {
  return (
    <Card className="flex items-center gap-4">
      {icon && (
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-brand-50 text-xl">
          {icon}
        </div>
      )}
      <div>
        <p className="text-sm text-slate-500">{label}</p>
        <p className="text-2xl font-bold text-slate-900">{value}</p>
        {trend && (
          <p
            className={cn(
              "mt-0.5 text-xs font-medium",
              trend.direction === "up" ? "text-emerald-600" : "text-red-500",
            )}
          >
            {trend.direction === "up" ? "▲" : "▼"} {trend.label}
          </p>
        )}
      </div>
    </Card>
  );
}
