package com.buzzanalysis.application.rag.dto;

import java.util.List;

/** RAG質問応答結果。{@code sources}は回答生成の根拠として取得したドキュメント一覧（類似度降順）。 */
public record RagQueryResultDto(String answer, List<RagDocumentDto> sources) {
}
