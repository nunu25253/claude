package com.buzzanalysis.domain.rag;

/** RAGドキュメントの索引元種別（Phase16）。{@code OTHER}は既存集約に紐付かない任意テキスト用。 */
public enum RagSourceType {
    ANALYSIS_RESULT,
    EVALUATION,
    TREND_REPORT,
    PROPOSAL,
    OTHER
}
