package com.buzzanalysis.application.rag;

import com.buzzanalysis.application.rag.dto.RagDocumentDto;
import com.buzzanalysis.application.rag.dto.RagIndexRequest;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.rag.RagDocument;
import com.buzzanalysis.domain.rag.RagDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 「RAG索引登録」ユースケース（Phase16）。テキストを明示的に索引化する（自動索引化はしない。
 * 設計docのセルフレビュー参照）。Embedding生成はPhase3の{@link EmbeddingClient}を再利用する。
 */
@Service
public class RagIndexingApplicationService {

    private final EmbeddingClient embeddingClient;
    private final RagDocumentRepository ragDocumentRepository;

    public RagIndexingApplicationService(EmbeddingClient embeddingClient, RagDocumentRepository ragDocumentRepository) {
        this.embeddingClient = embeddingClient;
        this.ragDocumentRepository = ragDocumentRepository;
    }

    @Transactional
    public RagDocumentDto index(RagIndexRequest request) {
        EmbeddingResult embeddingResult = embeddingClient.embed(request.contentText());

        RagDocument document = RagDocument.builder()
                .id(UUID.randomUUID())
                .sourceType(request.sourceType())
                .sourceId(request.sourceId())
                .contentText(request.contentText())
                .vector(embeddingResult.vector())
                .model(embeddingResult.model())
                .dimensions(embeddingResult.dimensions())
                .createdAt(OffsetDateTime.now())
                .build();

        return RagDocumentDto.from(ragDocumentRepository.save(document));
    }
}
