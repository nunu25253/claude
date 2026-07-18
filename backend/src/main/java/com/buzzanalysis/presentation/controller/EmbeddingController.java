package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.embedding.EmbeddingGenerationApplicationService;
import com.buzzanalysis.application.embedding.dto.EmbeddingDto;
import com.buzzanalysis.application.embedding.dto.EmbeddingGenerationSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Embedding生成API（AIマーケティングOS Phase3）。
 * 本文・ハッシュタグをOpenAI Embeddings APIでベクトル化し、pgvectorへ保存する
 * （タイトル・コメント要約は現状データ未収集のため非対応。docs/phases/phase3_embeddings.md参照）。
 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Embeddings", description = "Embedding生成・取得（AIマーケティングOS Phase3）")
public class EmbeddingController {

    private final EmbeddingGenerationApplicationService embeddingGenerationApplicationService;

    public EmbeddingController(EmbeddingGenerationApplicationService embeddingGenerationApplicationService) {
        this.embeddingGenerationApplicationService = embeddingGenerationApplicationService;
    }

    @Operation(summary = "投稿のEmbedding生成",
            description = "本文・ハッシュタグのEmbeddingを生成/再生成する。前回生成時から本文が変わっていない対象は"
                    + "API呼び出しをスキップする（コスト削減）。")
    @PostMapping("/{postId}/embeddings/generate")
    public ResponseEntity<EmbeddingGenerationSummaryDto> generate(@PathVariable UUID postId) {
        return ResponseEntity.ok(embeddingGenerationApplicationService.generateForPost(postId));
    }

    @Operation(summary = "投稿のEmbedding一覧取得")
    @GetMapping("/{postId}/embeddings")
    public ResponseEntity<List<EmbeddingDto>> getEmbeddings(@PathVariable UUID postId) {
        return ResponseEntity.ok(embeddingGenerationApplicationService.getEmbeddings(postId));
    }
}
