package com.buzzanalysis.application.rag;

import com.buzzanalysis.application.rag.dto.RagDocumentDto;
import com.buzzanalysis.application.rag.dto.RagQueryRequest;
import com.buzzanalysis.application.rag.dto.RagQueryResultDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.rag.RagDocument;
import com.buzzanalysis.domain.rag.RagDocumentRepository;
import com.buzzanalysis.domain.rag.RagSimilarityMatch;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final UsageQuotaService usageQuotaService;

    public RagQueryApplicationService(EmbeddingClient embeddingClient, RagDocumentRepository ragDocumentRepository,
                                       AiRagAnswerPort aiRagAnswerPort, UsageQuotaService usageQuotaService) {
        this.embeddingClient = embeddingClient;
        this.ragDocumentRepository = ragDocumentRepository;
        this.aiRagAnswerPort = aiRagAnswerPort;
        this.usageQuotaService = usageQuotaService;
    }

    @Transactional(readOnly = true)
    public RagQueryResultDto query(RagQueryRequest request, UUID requestingUserId) {
        if (!usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }
        int topK = resolveTopK(request.topK());

        EmbeddingResult queryEmbedding = embeddingClient.embed(request.question());
        List<RagSimilarityMatch> matches = ragDocumentRepository.findNearest(queryEmbedding.vector(), requestingUserId, topK);

        // 類似度上位の件数分findByIdをループで叩くN+1クエリになっていたため、まとめてバッチ取得し
        // 類似度順を保って並べ直す(SavedAnalysisApplicationService.listと同じ方針)。
        List<UUID> documentIds = matches.stream().map(RagSimilarityMatch::documentId).toList();
        Map<UUID, RagDocumentDto> documentsById = ragDocumentRepository.findByIdIn(documentIds).stream()
                .collect(Collectors.toMap(RagDocument::getId, RagDocumentDto::from));
        List<RagDocumentDto> sources = documentIds.stream().map(documentsById::get).filter(Objects::nonNull).toList();

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
