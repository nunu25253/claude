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
import { formatDate, formatPercent } from "@/lib/utils";

interface EngagementTrendChartProps {
  data: { date: string; avgEngagementRate: number }[];
}

export function EngagementTrendChart({ data }: EngagementTrendChartProps) {
  return (
    <ResponsiveContainer width="100%" height={260}>
      <LineChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
        <XAxis
          dataKey="date"
          tickFormatter={(v: string) => formatDate(v)}
          tick={{ fontSize: 12, fill: "#64748b" }}
        />
        <YAxis
          tickFormatter={(v: number) => formatPercent(v, 0)}
          tick={{ fontSize: 12, fill: "#64748b" }}
          width={48}
        />
        <Tooltip
          formatter={(value: number) => formatPercent(value)}
          labelFormatter={(label: string) => formatDate(label)}
        />
        <Line
          type="monotone"
          dataKey="avgEngagementRate"
          stroke="#4a63f5"
          strokeWidth={2}
          dot={false}
        />
      </LineChart>
    </ResponsiveContainer>
  );
}
