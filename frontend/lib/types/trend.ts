import type { Genre, Platform } from "./common";
import type { Post } from "./post";

export interface TrendHashtag {
  tag: string;
  platform: Platform;
  genre?: Genre;
  postCount: number;
  growthRate: number; // 前期間比の伸び率 (%)
}

export interface TrendResponse {
  hashtags: TrendHashtag[];
  posts: Post[];
}

export interface TrendQueryParams {
  platform?: Platform;
  genre?: Genre;
}
