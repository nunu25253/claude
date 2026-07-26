package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.semanticsearch.SemanticSearchApplicationService;
import com.buzzanalysis.application.semanticsearch.dto.SemanticSearchResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 意味検索API（AIマーケティングOS Phase4）。文字列としては一致しない意味的に近い投稿も検索できる
 * （例:「楽天カード」で「ポイ活」「SPU」等の投稿もヒット）。Phase3でBODY Embeddingが
 * 生成済みの投稿のみが検索対象となる。
 */
@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "SemanticSearch", description = "意味検索（Embedding + pgvectorコサイン類似度、AIマーケティングOS Phase4）")
public class SemanticSearchController {

    private final SemanticSearchApplicationService semanticSearchApplicationService;

    public SemanticSearchController(SemanticSearchApplicationService semanticSearchApplicationService) {
        this.semanticSearchApplicationService = semanticSearchApplicationService;
    }

    @Operation(summary = "意味検索", description = "キーワードをEmbedding化し、コサイン類似度が近い投稿を一致率付きで返す")
    @GetMapping("/semantic")
    public ResponseEntity<List<SemanticSearchResultDto>> semanticSearch(
            @Parameter(description = "検索キーワード") @RequestParam String keyword,
            @Parameter(description = "取得件数上限") @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(semanticSearchApplicationService.search(keyword, limit));
    }
}
