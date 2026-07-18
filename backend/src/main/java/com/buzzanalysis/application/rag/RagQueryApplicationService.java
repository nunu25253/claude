package com.buzzanalysis.application.rag;

import com.buzzanalysis.application.rag.dto.RagDocumentDto;
import com.buzzanalysis.application.rag.dto.RagQueryRequest;
import com.buzzanalysis.application.rag.dto.RagQueryResultDto;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.rag.RagDocumentRepository;
import com.buzzanalysis.domain.rag.RagSimilarityMatch;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 「RAG質問応答」ユースケース（Phase16）。質問文をEmbedding化し、索引済みドキュメントの中から
 * 類似度上位を取得した上で、それらを根拠としたAI回答を生成する。
 */
@Service
public class RagQueryApplicationService {

    private static final int DEFAULT_TOP_K = 5;
    private static final int MAX_TOP_K = 20;

    private final EmbeddingClient embeddingClient;
    private final RagDocumentRepository ragDocumentRepository;
    private final AiRagAnswerPort aiRagAnswerPort;

    public RagQueryApplicationService(EmbeddingClient embeddingClient, RagDocumentRepository ragDocumentRepository,
                                       AiRagAnswerPort aiRagAnswerPort) {
        this.embeddingClient = embeddingClient;
        this.ragDocumentRepository = ragDocumentRepository;
        this.aiRagAnswerPort = aiRagAnswerPort;
    }

    @Transactional(readOnly = true)
    public RagQueryResultDto query(RagQueryRequest request) {
        int topK = resolveTopK(request.topK());

        EmbeddingResult queryEmbedding = embeddingClient.embed(request.question());
        List<RagSimilarityMatch> matches = ragDocumentRepository.findNearest(queryEmbedding.vector(), topK);

        List<RagDocumentDto> sources = new ArrayList<>();
        for (RagSimilarityMatch match : matches) {
            ragDocumentRepository.findById(match.documentId())
                    .map(RagDocumentDto::from)
                    .ifPresent(sources::add);
        }

        String answer = aiRagAnswerPort.generateAnswer(request.question(), sources);
        return new RagQueryResultDto(answer, sources);
    }

    private int resolveTopK(Integer requested) {
        if (requested == null || requested <= 0) {
            return DEFAULT_TOP_K;
        }
        return Math.min(requested, MAX_TOP_K);
    }
}
