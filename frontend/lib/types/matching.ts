import type { ContentFormat } from "./commonality";
import type { Platform } from "./common";
import type { Post } from "./post";

/** ユーザー条件分析(案件マッチ度チェック, AIマーケティングOS Phase6)関連の型定義。 */

export interface MatchingConditionRequest {
  keyword?: string;
  productName?: string;
  brand?: string;
  aspOfferName?: string;
  genre?: string;
  subGenre?: string;
  targetAgeRange?: string;
  targetGender?: string;
  postFormat?: ContentFormat;
  platform?: Platform;
  videoDurationSeconds?: number;
  purpose?: string;
}

export interface MatchRateResult {
  post: Post;
  matchRatePercent: number;
  breakdown: Record<string, number>;
}
