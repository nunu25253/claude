import type { Genre, Platform } from "./common";

// 投稿の基本メタデータ（SNSから取得した生データ）
export interface Post {
  id: string;
  url: string;
  platform: Platform;
  genre?: Genre;
  accountId: string;
  accountName: string;
  accountHandle: string;
  accountAvatarUrl?: string;
  thumbnailUrl?: string;
  caption: string;
  hashtags: string[];
  postedAt: string; // ISO8601
  likeCount: number;
  commentCount: number;
  shareCount: number;
  saveCount?: number;
  viewCount?: number;
  videoDurationSeconds?: number; // 動画の場合のみ
  carouselCount?: number; // カルーセル投稿の枚数
  characterCount?: number; // キャプション文字数
  buzzScore?: number; // 0-100
}

// BuzzScore の内訳（レーダーチャート等での可視化用）
export interface BuzzScoreBreakdown {
  total: number; // 0-100
  engagementScore: number;
  velocityScore: number; // 伸びる速度
  shareabilityScore: number;
  retentionScore: number; // 動画視聴維持率などから算出
}

// 感情分析結果
export interface SentimentAnalysis {
  overallSentiment: "POSITIVE" | "NEUTRAL" | "NEGATIVE" | "MIXED";
  positiveRatio: number; // 0-1
  negativeRatio: number;
  neutralRatio: number;
  emotionTags: string[]; // 例: ["共感", "驚き", "癒やし"]
}

// 動画構成の1シーン
export interface VideoStructureSegment {
  order: number;
  startSeconds: number;
  endSeconds: number;
  label: string; // 例: "フック", "本編導入", "オチ"
  description: string;
}

// カルーセル1枚ごとの構成
export interface CarouselStructureSlide {
  order: number;
  role: string; // 例: "表紙", "問題提起", "解決策", "CTA"
  description: string;
}

// ハッシュタグ分析結果
export interface HashtagAnalysisItem {
  tag: string;
  postCount: number; // このタグを使った投稿数（母数）
  avgEngagementRate: number;
  isRecommended: boolean;
}

// AIによる投稿分析結果本体
export interface PostAnalysis {
  id: string;
  postId: string;
  buzzScore: BuzzScoreBreakdown;
  viralReasons: string[]; // 伸びた理由
  targetAudience: string[]; // ターゲット層
  hooks: string[]; // 冒頭のフック分析
  callToActions: string[]; // CTA分析
  sentiment: SentimentAnalysis;
  videoStructure?: VideoStructureSegment[];
  carouselStructure?: CarouselStructureSlide[];
  titleAnalysis: {
    summary: string;
    strengths: string[];
    weaknesses: string[];
  };
  captionAnalysis: {
    summary: string;
    tone: string;
    readabilityScore: number; // 0-100
  };
  postingTimeAnalysis: {
    postedAt: string;
    isOptimalTiming: boolean;
    recommendedTimeWindows: string[]; // 例: ["19:00-21:00", "土日午前"]
    comment: string;
  };
  hashtagAnalysis: HashtagAnalysisItem[];
  improvementSuggestions: string[]; // 改善案
  similarPosts: Post[]; // 類似の伸びている投稿提案
  createdAt: string;
}

// 投稿分析リクエスト（URL入力フォーム）
export interface AnalyzePostRequest {
  url: string;
}

// 分析APIのレスポンス（投稿データ + AI分析結果）
export interface AnalyzePostResponse {
  post: Post;
  analysis: PostAnalysis;
}

export interface PostSearchParams {
  keyword?: string;
  hashtag?: string;
  account?: string;
  platform?: Platform;
  genre?: Genre;
  page?: number;
  size?: number;
  sort?: string;
}
