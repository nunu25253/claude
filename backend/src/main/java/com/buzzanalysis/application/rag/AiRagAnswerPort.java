package com.buzzanalysis.application.rag;

import com.buzzanalysis.application.rag.dto.RagDocumentDto;

import java.util.List;

/**
 * OpenAI等のLLMを用いた「RAG回答生成」を抽象化するポート（Phase16）。実装はinfrastructure層に置く。
 * 取得済みドキュメント（{@code context}）を根拠として、質問に対する回答を生成する。
 */
public interface AiRagAnswerPort {

    String generateAnswer(String question, List<RagDocumentDto> context);
}
