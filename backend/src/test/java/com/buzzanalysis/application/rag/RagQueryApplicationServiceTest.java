package com.buzzanalysis.application.rag;

import com.buzzanalysis.application.rag.dto.RagQueryRequest;
import com.buzzanalysis.application.rag.dto.RagQueryResultDto;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.rag.RagDocument;
import com.buzzanalysis.domain.rag.RagDocumentRepository;
import com.buzzanalysis.domain.rag.RagSimilarityMatch;
import com.buzzanalysis.domain.rag.RagSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagQueryApplicationServiceTest {

    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private RagDocumentRepository ragDocumentRepository;
    @Mock
    private AiRagAnswerPort aiRagAnswerPort;

    private RagQueryApplicationService service;

    @BeforeEach
    void setUp() {
        service = new RagQueryApplicationService(embeddingClient, ragDocumentRepository, aiRagAnswerPort);
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

        RagQueryResultDto result = service.query(new RagQueryRequest("質問文", null));

        assertThat(result.answer()).isEqualTo("生成された回答");
        assertThat(result.sources()).hasSize(1);
        assertThat(result.sources().get(0).contentText()).isEqualTo("過去の評価内容");
    }

    @Test
    void query_clampsTopKToMax20() {
        when(embeddingClient.embed(any())).thenReturn(new EmbeddingResult(new float[]{0.1f}, "model", 1));
        when(ragDocumentRepository.findNearest(any(), eq(20))).thenReturn(List.of());
        when(aiRagAnswerPort.generateAnswer(any(), any())).thenReturn("回答");

        service.query(new RagQueryRequest("質問", 100));

        verify(ragDocumentRepository).findNearest(any(), eq(20));
    }

    @Test
    void query_skipsMissingDocuments_gracefully() {
        UUID docId = UUID.randomUUID();
        when(embeddingClient.embed(any())).thenReturn(new EmbeddingResult(new float[]{0.1f}, "model", 1));
        when(ragDocumentRepository.findNearest(any(), anyInt())).thenReturn(List.of(new RagSimilarityMatch(docId, 0.5)));
        when(ragDocumentRepository.findById(docId)).thenReturn(Optional.empty());
        when(aiRagAnswerPort.generateAnswer(any(), any())).thenReturn("回答");

        RagQueryResultDto result = service.query(new RagQueryRequest("質問", null));

        assertThat(result.sources()).isEmpty();
    }
}
