package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.preprocessing.PreprocessingApplicationService;
import com.buzzanalysis.application.preprocessing.dto.PreprocessedPostDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * AI分析用前処理API（AIマーケティングOS Phase2）。
 * テキストクレンジング・言語判定・ハッシュタグ/メンション抽出・投稿時間解析・動画時間解析・
 * コンテンツ形式判定を行った結果を返す。すべて決定的なルールで導出され、AI生成値は含まない。
 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Preprocessing", description = "AI分析用前処理（テキストクレンジング・言語判定等）")
public class PreprocessingController {

    private final PreprocessingApplicationService preprocessingApplicationService;

    public PreprocessingController(PreprocessingApplicationService preprocessingApplicationService) {
        this.preprocessingApplicationService = preprocessingApplicationService;
    }

    @Operation(summary = "投稿の前処理結果取得",
            description = "クレンジング済み本文、判定言語、ハッシュタグ/メンション、投稿時間解析、動画時間解析、"
                    + "コンテンツ形式を返す。")
    @GetMapping("/{postId}/preprocessed")
    public ResponseEntity<PreprocessedPostDto> getPreprocessed(@PathVariable UUID postId) {
        return ResponseEntity.ok(preprocessingApplicationService.preprocess(postId));
    }
}
