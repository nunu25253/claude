package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.rag.AiRagAnswerPort;
import com.buzzanalysis.application.rag.dto.RagDocumentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link AiRagAnswerPort} のOpenAI実装。質問と、類似度上位で取得済みのドキュメント群を渡し、
 * それらを根拠とした回答をAIに生成させる（推測での回答を避け、根拠データの範囲内で答えるよう指示する）。
 * APIキー未設定時・呼び出し失敗時は、取得済みドキュメントの抜粋を機械的に連結したフォールバックを返す。
 */
@Service
public class OpenAiRagAnswerService implements AiRagAnswerPort {

    private static final Logger log = LoggerFactory.getLogger(OpenAiRagAnswerService.class);
    private static final int EXCERPT_MAX_LENGTH = 200;

    private final OpenAiClient openAiClient;
    private final OpenAiProperties properties;

    public OpenAiRagAnswerService(OpenAiClient openAiClient, OpenAiProperties properties) {
        this.openAiClient = openAiClient;
        this.properties = properties;
    }

    private static final String SYSTEM_PROMPT = """
            あなたはSNSマーケティングの過去データに基づいて回答するアシスタントです。以下に与えられる
            「参考ドキュメント」の内容のみを根拠に質問へ回答してください。参考ドキュメントに書かれていない
            ことは推測で補わず、その旨を明示してください。
            """;

    @Override
    public String generateAnswer(String question, List<RagDocumentDto> context) {
        if (context.isEmpty()) {
            return "参考ドキュメントが見つからなかったため、回答できません。先にRAG索引登録を行ってください。";
        }
        if (!properties.isConfigured()) {
            log.info("OpenAI API key not configured; using extractive fallback RAG answer");
            return fallbackAnswer(context);
        }
        try {
            String userPrompt = buildPrompt(question, context);
            return openAiClient.chatCompleteAsPlainText(SYSTEM_PROMPT, userPrompt);
        } catch (Exception e) {
            log.warn("OpenAI RAG answer generation failed, falling back to extractive answer: {}", e.getMessage());
            return fallbackAnswer(context);
        }
    }

    private String buildPrompt(String question, List<RagDocumentDto> context) {
        StringBuilder sb = new StringBuilder("質問: ").append(question).append("\n\n参考ドキュメント:\n");
        for (int i = 0; i < context.size(); i++) {
            RagDocumentDto doc = context.get(i);
            sb.append(i + 1).append(". [").append(doc.sourceType()).append("] ")
                    .append(truncate(doc.contentText())).append("\n");
        }
        return sb.toString();
    }

    private String fallbackAnswer(List<RagDocumentDto> context) {
        StringBuilder sb = new StringBuilder("（抽出型簡易回答: OpenAI未接続のため参考ドキュメントの抜粋をそのまま提示）\n");
        for (int i = 0; i < context.size(); i++) {
            RagDocumentDto doc = context.get(i);
            sb.append(i + 1).append(". [").append(doc.sourceType()).append("] ")
                    .append(truncate(doc.contentText())).append("\n");
        }
        return sb.toString();
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        return text.length() <= EXCERPT_MAX_LENGTH ? text : text.substring(0, EXCERPT_MAX_LENGTH) + "...";
    }
}
