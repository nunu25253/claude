/** 共通点分析(勝ちパターン抽出, Phase8)関連の型定義。 */

export type ContentFormat = "SHORT_VIDEO" | "LONG_VIDEO" | "SINGLE_IMAGE" | "MULTI_IMAGE_CAROUSEL" | "TEXT_ONLY";

export interface CommonalityAnalysisRequest {
  postIds: string[];
}

export interface CommonalityAnalysisResult {
  totalPostCount: number;
  aiSampleSize: number;
  commonHashtags: string[];
  commonVideoDurationSeconds?: number;
  commonPostingHour?: number;
  commonContentFormat?: ContentFormat;
  commonTitlePattern?: string;
  commonHookPattern?: string;
  commonCtaPattern?: string;
  commonStructurePattern?: string;
  commonTargetPattern?: string;
}
