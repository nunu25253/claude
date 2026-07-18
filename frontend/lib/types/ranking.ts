import type { Genre, Platform } from "./common";
import type { Post } from "./post";

export type RankingType = "trending" | "weekly" | "monthly";

export interface RankingItem {
  rank: number;
  post: Post;
  rankChange?: number; // 前回順位からの変動（+上昇 / -下降）
}

export interface RankingQueryParams {
  type: RankingType;
  genre?: Genre;
  platform?: Platform;
}
