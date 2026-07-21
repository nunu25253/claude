"use client";

import {
  PolarAngleAxis,
  PolarGrid,
  Radar,
  RadarChart,
  ResponsiveContainer,
} from "recharts";

interface BuzzScoreRadarChartProps {
  breakdown: Record<string, number>;
}

// バックエンドのBuzzScoreStrategy実装(engagementRate/viewCount/commentRate/postFormat/hashtag/
// postTiming/contentStructure/aiAnalysis)に対応する日本語ラベル。未知のキーは英語名のまま表示する。
const METRIC_LABELS: Record<string, string> = {
  engagementRate: "エンゲージメント率",
  viewCount: "再生数",
  commentRate: "コメント率",
  postFormat: "投稿形式",
  hashtag: "ハッシュタグ",
  postTiming: "投稿時間",
  contentStructure: "構成",
  aiAnalysis: "AI分析",
};

export function BuzzScoreRadarChart({ breakdown }: BuzzScoreRadarChartProps) {
  const data = Object.entries(breakdown).map(([key, value]) => ({
    metric: METRIC_LABELS[key] ?? key,
    value,
  }));

  if (data.length === 0) {
    return null;
  }

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
