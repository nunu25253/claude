"use client";

import {
  PolarAngleAxis,
  PolarGrid,
  Radar,
  RadarChart,
  ResponsiveContainer,
} from "recharts";
import type { BuzzScoreBreakdown } from "@/lib/types";

interface BuzzScoreRadarChartProps {
  breakdown: BuzzScoreBreakdown;
}

export function BuzzScoreRadarChart({ breakdown }: BuzzScoreRadarChartProps) {
  const data = [
    { metric: "エンゲージメント", value: breakdown.engagementScore },
    { metric: "伸び速度", value: breakdown.velocityScore },
    { metric: "拡散性", value: breakdown.shareabilityScore },
    { metric: "視聴維持", value: breakdown.retentionScore },
  ];

  return (
    <ResponsiveContainer width="100%" height={260}>
      <RadarChart data={data} outerRadius="75%">
        <PolarGrid stroke="#e2e8f0" />
        <PolarAngleAxis dataKey="metric" tick={{ fontSize: 12, fill: "#475569" }} />
        <Radar
          name="スコア"
          dataKey="value"
          stroke="#4a63f5"
          fill="#4a63f5"
          fillOpacity={0.35}
        />
      </RadarChart>
    </ResponsiveContainer>
  );
}
