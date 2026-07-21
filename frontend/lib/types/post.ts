import type { Genre, Platform } from "./common";

// 投稿の基本メタデータ（SNSから取得した生データ）。
// accountId/accountName/accountHandle/thumbnailUrl/genre/buzzScore等は/trendsのように
// フロントエンド向けに整形して返すエンドポイントのみ埋まる（/posts/analyze・/rankings等のバックエンドの
// 生PostDtoにはこれらが無く、代わりにauthorName/publishedAtが返る）。両対応のため両方optionalにしてある。
export interface Post {
  id: string;
  url: string;
  platform: Platform;
  genre?: Genre;
  accountId?: string;
  accountName?: string;
  accountHandle?: string;
  accountAvatarUrl?: string;
  authorName?: string; // 生PostDtoの投稿者名(accountName/accountHandleが無い場合のフォールバック)
  thumbnailUrl?: string;
  caption: string;
  hashtags: string[];
  postedAt?: string; // ISO8601
  publishedAt?: string; // 生PostDtoのフィールド名(postedAtが無い場合のフォールバック)
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

// BuzzScoreの算出結果。内訳(breakdown)はStrategyパターンで算出される評価項目ごとのスコアで、
// 項目の種類・数はバックエンドの実装に依存するため固定フィールドにせずRecordで受ける。
export interface BuzzScoreResult {
  postId: string;
  totalScore: number; // 0-100
  breakdown: Record<string, number>;
}

// AIによる投稿分析結果本体。バックエンドのAnalysisResultDtoに対応する
// (各項目はAIが生成した自然文であり、配列や構造化オブジェクトではない)。
export interface PostAnalysis {
  id: string;
  postId: string;
  genre?: string;
  subGenre?: string;
  whyItWentViral: string; // 伸びた理由
  targetAudience: string; // ターゲット層
  postPurpose?: string;
  hook: string; // 冒頭のフック分析
  callToAction: string; // CTA分析
  postStructureAnalysis?: string;
  sentimentAnalysis: string; // 感情分析
  videoStructureAnalysis?: string; // 動画構成分析(動画投稿でない場合は「対象外です」等の文言)
  carouselStructureAnalysis?: string; // カルーセル構成分析(カルーセル投稿でない場合は同上)
  titleAnalysis: string;
  textAnalysis: string; // 文章(キャプション)分析
  postingTimeAnalysis: string; // 投稿時間分析
  hashtagAnalysis: string;
  strengths: string;
  weaknesses: string;
  improvementSuggestions: string; // 改善案
}

// 投稿分析リクエスト（URL入力フォーム）。バックエンドのAnalyzePostRequestに合わせフィールド名はpostUrl。
export interface AnalyzePostRequest {
  postUrl: string;
}

// 分析APIのレスポンス（投稿データ + AI分析結果 + BuzzScore + 類似投稿）
export interface AnalyzePostResponse {
  post: Post;
  analysis: PostAnalysis;
  buzzScore: BuzzScoreResult;
  similarPosts: Post[];
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
