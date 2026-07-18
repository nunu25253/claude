package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.rag.RagIndexingApplicationService;
import com.buzzanalysis.application.rag.RagQueryApplicationService;
import com.buzzanalysis.application.rag.dto.RagDocumentDto;
import com.buzzanalysis.application.rag.dto.RagIndexRequest;
import com.buzzanalysis.application.rag.dto.RagQueryRequest;
import com.buzzanalysis.application.rag.dto.RagQueryResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RAG API（AIマーケティングOS Phase16）。過去の分析結果・投稿評価・トレンドレポート等を明示的に
 * 索引登録し、自然文の質問に対してAIが索引済みデータを根拠に回答する。
 */
@RestController
@RequestMapping("/api/v1/rag")
@Tag(name = "Rag", description = "RAG（AIマーケティングOS Phase16）")
public class RagController {

    private final RagIndexingApplicationService ragIndexingApplicationService;
    private final RagQueryApplicationService ragQueryApplicationService;

    public RagController(RagIndexingApplicationService ragIndexingApplicationService,
                          RagQueryApplicationService ragQueryApplicationService) {
        this.ragIndexingApplicationService = ragIndexingApplicationService;
        this.ragQueryApplicationService = ragQueryApplicationService;
    }

    @Operation(summary = "テキストのRAG索引登録",
            description = "分析結果・評価・トレンドサマリー等のテキストを明示的にRAG検索対象として索引登録する。")
    @PostMapping("/index")
    public ResponseEntity<RagDocumentDto> index(@RequestBody RagIndexRequest request) {
        return ResponseEntity.ok(ragIndexingApplicationService.index(request));
    }

    @Operation(summary = "RAG質問応答",
            description = "索引済みドキュメントの中から類似度上位(既定5件、上限20件)を取得し、それらを根拠にAIが回答する。")
    @PostMapping("/query")
    public ResponseEntity<RagQueryResultDto> query(@RequestBody RagQueryRequest request) {
        return ResponseEntity.ok(ragQueryApplicationService.query(request));
    }
}
