package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.competitor.CompetitorAnalysisApplicationService;
import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** 競合アカウント分析API。 */
@RestController
@RequestMapping("/api/v1/competitors")
@Tag(name = "Competitors", description = "競合アカウント統計分析")
public class CompetitorController {

    private final CompetitorAnalysisApplicationService competitorAnalysisApplicationService;

    public CompetitorController(CompetitorAnalysisApplicationService competitorAnalysisApplicationService) {
        this.competitorAnalysisApplicationService = competitorAnalysisApplicationService;
    }

    @Operation(summary = "競合アカウント統計取得",
            description = "平均いいね数・平均コメント数・投稿頻度・投稿時間傾向・伸びる投稿ランキング等を返す")
    @GetMapping("/{accountId}/stats")
    public ResponseEntity<CompetitorStatsDto> getStats(@PathVariable UUID accountId) {
        return ResponseEntity.ok(competitorAnalysisApplicationService.getStats(accountId));
    }
}
