/** 投稿評価AI（Phase14）関連の型定義。 */

export interface PostEvaluationRequest {
  proposalId?: string;
  title: string;
  hookText?: string;
  structureText?: string;
  ctaText?: string;
  targetAudienceText?: string;
}

export interface ContentEvaluation {
  id: string;
  proposalId?: string;
  title: string;
  matchRatePercent?: number;
  targetAudienceEstimate?: string;
  improvementSuggestions: string[];
  hookImprovement?: string;
  ctaImprovement?: string;
  predictedScore: number;
  createdAt: string;
}
