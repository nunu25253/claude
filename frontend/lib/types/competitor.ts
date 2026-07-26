import type { Platform } from "./common";
import type { Post } from "./post";

export interface CompetitorAccountSummary {
  accountId: string;
  platform: Platform;
  handle: string;
  displayName: string;
  avatarUrl?: string;
  followerCount: number;
  postCount: number;
}

// 投稿時間帯の分布（曜日 x 時間帯ヒートマップ用）
export interface PostingTimeDistributionItem {
  dayOfWeek: number; // 0=日曜 ... 6=土曜
  hour: number; // 0-23
  postCount: number;
}

export interface CompetitorStats {
  account: CompetitorAccountSummary;
  avgLikeCount: number;
  avgCommentCount: number;
  avgShareCount: number;
  postFrequencyPerWeek: number;
  avgVideoDurationSeconds: number;
  avgCharacterCount: number;
  postingTimeDistribution: PostingTimeDistributionItem[];
  topGrowingPosts: Post[]; // 伸びる投稿ランキング
  engagementTrend: {
    date: string; // ISO date
    avgEngagementRate: number;
  }[];
}
