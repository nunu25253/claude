package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.trend.TrendAnalysisApplicationService;
import com.buzzanalysis.application.trend.dto.TrendAnalysisRequest;
import com.buzzanalysis.application.trend.dto.TrendReportDto;
import com.buzzanalysis.domain.platform.Platform;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * トレンド分析API（AIマーケティングOS Phase15）。直近ウィンドウとベースラインウィンドウの2期間比較で
 * 急上昇しているハッシュタグ/ジャンル/コンテンツ形式を検出する（統計計算はすべて決定的。AIはサマリー
 * 生成のみに使用）。日次自動実行は{@code batch.trend.enabled=true}で有効化できる（既定は無効）。
 */
@RestController
@RequestMapping("/api/v1/trends")
@Tag(name = "Trends", description = "トレンド分析（AIマーケティングOS Phase15）")
public class TrendController {

    private final TrendAnalysisApplicationService trendAnalysisApplicationService;

    public TrendController(TrendAnalysisApplicationService trendAnalysisApplicationService) {
        this.trendAnalysisApplicationService = trendAnalysisApplicationService;
    }

    @Operation(summary = "トレンド分析の実行",
            description = "platform省略時は全プラットフォーム対象。recentWindowDays/baselineWindowDays省略時は既定値(7日/21日)。")
    @PostMapping("/analyze")
    public ResponseEntity<TrendReportDto> analyze(@RequestBody TrendAnalysisRequest request) {
        return ResponseEntity.ok(trendAnalysisApplicationService.analyze(request));
    }

    @Operation(summary = "最新トレンドレポートの取得", description = "platform省略時は全プラットフォーム対象のレポートを検索する。")
    @GetMapping("/latest")
    public ResponseEntity<List<TrendReportDto>> findLatest(
            @RequestParam(required = false) Platform platform,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(trendAnalysisApplicationService.findLatest(platform, limit));
    }
}
