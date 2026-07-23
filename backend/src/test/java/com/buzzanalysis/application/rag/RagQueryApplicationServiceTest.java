package com.buzzanalysis.application.rag;

import com.buzzanalysis.application.rag.dto.RagQueryRequest;
import com.buzzanalysis.application.rag.dto.RagQueryResultDto;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.rag.RagDocument;
import com.buzzanalysis.domain.rag.RagDocumentRepository;
import com.buzzanalysis.domain.rag.RagSimilarityMatch;
import com.buzzanalysis.domain.rag.RagSourceType;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RagQueryApplicationServiceTest {

    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private RagDocumentRepository ragDocumentRepository;
    @Mock
    private AiRagAnswerPort aiRagAnswerPort;
    @Mock
    private UsageQuotaService usageQuotaService;

    private RagQueryApplicationService service;
    private UUID requestingUserId;

    @BeforeEach
    void setUp() {
        service = new RagQueryApplicationService(embeddingClient, ragDocumentRepository, aiRagAnswerPort,
                usageQuotaService);
        requestingUserId = UUID.randomUUID();
        when(usageQuotaService.tryConsume(any())).thenReturn(true);
    }

    @Test
    void query_throwsBusinessRuleViolation_whenDailyUsageQuotaExceeded() {
        when(usageQuotaService.tryConsume(requestingUserId)).thenReturn(false);

        assertThatThrownBy(() -> service.query(new RagQueryRequest("質問", null), requestingUserId))
                .isInstanceOf(BusinessRuleViolationException.class);
        verifyNoInteractions(embeddingClient, ragDocumentRepository, aiRagAnswerPort);
    }

    @Test
    void query_retrievesNearestDocuments_andGeneratesAnswer() {
        UUID docId = UUID.randomUUID();
        when(embeddingClient.embed("質問文"))
                .thenReturn(new EmbeddingResult(new float[]{0.1f, 0.2f}, "text-embedding-3-small", 2));
        when(ragDocumentRepository.findNearest(any(), eq(5)))
                .thenReturn(List.of(new RagSimilarityMatch(docId, 0.9)));
        RagDocument document = RagDocument.builder().id(docId).sourceType(RagSourceType.EVALUATION)
                .contentText("過去の評価内容").createdAt(OffsetDateTime.now()).build();
        when(ragDocumentRepository.findById(docId)).thenReturn(Optional.of(document));
        when(aiRagAnswerPort.generateAnswer(eq("質問文"), any())).thenReturn("生成された回答");

        RagQueryResultDto result = service.query(new RagQueryRequest("質問文", null), requestingUserId);

        assertThat(result.answer()).isEqualTo("生成された回答");
        assertThat(result.sources()).hasSize(1);
        assertThat(result.sources().get(0).contentText()).isEqualTo("過去の評価内容");
    }

    @Test
    void query_clampsTopKToMax20() {
        when(embeddingClient.embed(any())).thenReturn(new EmbeddingResult(new float[]{0.1f}, "model", 1));
        when(ragDocumentRepository.findNearest(any(), eq(20))).thenReturn(List.of());
        when(aiRagAnswerPort.generateAnswer(any(), any())).thenReturn("回答");

        service.query(new RagQueryRequest("質問", 100), requestingUserId);

        verify(ragDocumentRepository).findNearest(any(), eq(20));
    }

    @Test
    void query_skipsMissingDocuments_gracefully() {
        UUID docId = UUID.randomUUID();
        when(embeddingClient.embed(any())).thenReturn(new EmbeddingResult(new float[]{0.1f}, "model", 1));
        when(ragDocumentRepository.findNearest(any(), anyInt())).thenReturn(List.of(new RagSimilarityMatch(docId, 0.5)));
        when(ragDocumentRepository.findById(docId)).thenReturn(Optional.empty());
        when(aiRagAnswerPort.generateAnswer(any(), any())).thenReturn("回答");

        RagQueryResultDto result = service.query(new RagQueryRequest("質問", null), requestingUserId);

        assertThat(result.sources()).isEmpty();
    }
}
