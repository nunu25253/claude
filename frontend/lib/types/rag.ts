/** RAGアシスタント(過去の保存済み分析への質問応答, Phase16)関連の型定義。 */

export type RagSourceType = "ANALYSIS_RESULT" | "EVALUATION" | "TREND_REPORT" | "PROPOSAL" | "OTHER";

export interface RagQueryRequest {
  question: string;
  topK?: number;
}

export interface RagDocument {
  id: string;
  sourceType: RagSourceType;
  sourceId?: string;
  contentText: string;
  createdAt: string;
}

export interface RagQueryResult {
  answer: string;
  sources: RagDocument[];
}
