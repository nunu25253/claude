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
 * 「RAG索引登録」ユースケース（Phase16）。テキストを索引化する。索引ドキュメントは索引登録した
 * ユーザーに紐付き(userId)、検索時は必ずこのユーザーの範囲に絞り込まれる(他ユーザーへの漏洩防止)。
 * Embedding生成はPhase3の{@link EmbeddingClient}を再利用する。
 * 保存済み分析の作成時に{@code SavedAnalysisRagIndexingListener}から自動的に呼び出されるほか、
 * このAPIを直接叩いて任意のテキストを索引登録することもできる。
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
    public RagDocumentDto index(RagIndexRequest request, UUID requestingUserId) {
        EmbeddingResult embeddingResult = embeddingClient.embed(request.contentText());

        RagDocument document = RagDocument.builder()
                .id(UUID.randomUUID())
                .userId(requestingUserId)
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
