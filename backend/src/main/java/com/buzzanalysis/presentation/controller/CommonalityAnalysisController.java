package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.commonality.CommonalityAnalysisApplicationService;
import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisRequest;
import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 共通点分析API（AIマーケティングOS Phase8）。検索結果等の投稿群（最大100件）から、
 * 共通ハッシュタグ/動画時間/投稿時間/コンテンツ形式（統計）と共通タイトル/フック/CTA/構成/ターゲット（AI）を抽出する。
 */
@RestController
@RequestMapping("/api/v1/commonality")
@Tag(name = "Commonality", description = "共通点分析（AIマーケティングOS Phase8）")
public class CommonalityAnalysisController {

    private final CommonalityAnalysisApplicationService commonalityAnalysisApplicationService;

    public CommonalityAnalysisController(CommonalityAnalysisApplicationService commonalityAnalysisApplicationService) {
        this.commonalityAnalysisApplicationService = commonalityAnalysisApplicationService;
    }

    @Operation(summary = "投稿群の共通点分析",
            description = "投稿ID配列(最大100件)から共通パターンを抽出する。AIには要約スニペットを最大30件までサンプリングして渡す。")
    @PostMapping("/analyze")
    public ResponseEntity<CommonalityAnalysisResultDto> analyze(@RequestBody CommonalityAnalysisRequest request) {
        return ResponseEntity.ok(commonalityAnalysisApplicationService.analyze(request.postIds()));
    }
}
