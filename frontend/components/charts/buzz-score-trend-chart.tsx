"use client";

import {
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  CartesianGrid,
} from "recharts";
import { formatDateTime } from "@/lib/utils";
import type { BuzzScoreHistoryPoint } from "@/lib/types";

export function BuzzScoreTrendChart({ data }: { data: BuzzScoreHistoryPoint[] }) {
  return (
    <ResponsiveContainer width="100%" height={220}>
      <LineChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
        <XAxis
          dataKey="calculatedAt"
          tickFormatter={(v: string) => formatDateTime(v)}
          tick={{ fontSize: 12, fill: "#64748b" }}
        />
        <YAxis domain={[0, 100]} tick={{ fontSize: 12, fill: "#64748b" }} width={32} />
        <Tooltip
          formatter={(value: number) => value.toFixed(1)}
          labelFormatter={(label: string) => formatDateTime(label)}
        />
        <Line
          type="monotone"
          dataKey="totalScore"
          stroke="#4a63f5"
          strokeWidth={2}
          dot={{ r: 3 }}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
