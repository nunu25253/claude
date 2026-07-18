/**
 * AI企画（Phase10: 企画生成AI / Phase11: 台本生成AI / Phase12: カルーセル生成AI）関連の型定義。
 */

export type RecommendedFormat =
  | "SHORT_VIDEO"
  | "LONG_VIDEO"
  | "SINGLE_IMAGE"
  | "MULTI_IMAGE_CAROUSEL"
  | "TEXT_ONLY";

export interface ContentProposal {
  id: string;
  generationId: string;
  sequenceNumber: number;
  title: string;
  hookPattern?: string;
  structureSummary?: string;
  callToAction?: string;
  targetAudience?: string;
  genre?: string;
  recommendedFormat?: RecommendedFormat;
  reasoning?: string;
  createdAt: string;
}

export interface ProposalGenerationRequest {
  postIds: string[];
  count?: number;
}

export interface ScriptCut {
  cutNumber: number;
  startSecond: number;
  endSecond: number;
  narration?: string;
  telop?: string;
  visualDirection?: string;
}

export interface VideoScript {
  id: string;
  proposalId: string;
  durationSeconds: number;
  bgmImage?: string;
  callToAction?: string;
  cuts: ScriptCut[];
  createdAt: string;
}

export interface ScriptGenerationRequest {
  proposalId: string;
  durationSeconds: 30 | 60 | 90;
}

export type CarouselPageRole = "HOOK" | "EXPLANATION" | "CTA";

export interface CarouselPage {
  pageNumber: number;
  role: CarouselPageRole;
  headline: string;
  bodyText?: string;
  visualDirection?: string;
}

export interface Carousel {
  id: string;
  proposalId: string;
  pages: CarouselPage[];
  createdAt: string;
}

export interface CarouselGenerationRequest {
  proposalId: string;
}
