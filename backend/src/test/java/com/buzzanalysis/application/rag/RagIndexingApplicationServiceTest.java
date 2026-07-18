package com.buzzanalysis.application.rag;

import com.buzzanalysis.application.rag.dto.RagDocumentDto;
import com.buzzanalysis.application.rag.dto.RagIndexRequest;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.rag.RagDocumentRepository;
import com.buzzanalysis.domain.rag.RagSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagIndexingApplicationServiceTest {

    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private RagDocumentRepository ragDocumentRepository;

    private RagIndexingApplicationService service;

    @BeforeEach
    void setUp() {
        service = new RagIndexingApplicationService(embeddingClient, ragDocumentRepository);
    }

    @Test
    void index_embedsTextAndPersistsDocument() {
        UUID sourceId = UUID.randomUUID();
        when(embeddingClient.embed("過去の成功事例のテキスト"))
                .thenReturn(new EmbeddingResult(new float[]{0.1f, 0.2f}, "text-embedding-3-small", 2));
        when(ragDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RagDocumentDto result = service.index(
                new RagIndexRequest(RagSourceType.EVALUATION, sourceId, "過去の成功事例のテキスト"));

        assertThat(result.sourceType()).isEqualTo(RagSourceType.EVALUATION);
        assertThat(result.sourceId()).isEqualTo(sourceId);
        assertThat(result.contentText()).isEqualTo("過去の成功事例のテキスト");
    }
}
