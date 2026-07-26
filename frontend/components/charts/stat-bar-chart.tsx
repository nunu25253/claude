"use client";

import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { formatCompactNumber } from "@/lib/utils";

interface StatBarChartProps {
  data: { label: string; value: number }[];
  color?: string;
  valueFormatter?: (value: number) => string;
}

export function StatBarChart({
  data,
  color = "#4a63f5",
  valueFormatter = formatCompactNumber,
}: StatBarChartProps) {
  return (
    <ResponsiveContainer width="100%" height={260}>
      <BarChart data={data} margin={{ top: 8, right: 16, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
        <XAxis dataKey="label" tick={{ fontSize: 12, fill: "#64748b" }} />
        <YAxis tickFormatter={valueFormatter} tick={{ fontSize: 12, fill: "#64748b" }} width={48} />
        <Tooltip formatter={(value: number) => valueFormatter(value)} />
        <Bar dataKey="value" fill={color} radius={[6, 6, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  );
}
