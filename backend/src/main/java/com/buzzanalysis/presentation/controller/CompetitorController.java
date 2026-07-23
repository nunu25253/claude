package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.competitor.CompetitorAnalysisApplicationService;
import com.buzzanalysis.application.competitor.CompetitorComparisonApplicationService;
import com.buzzanalysis.application.competitor.dto.CompetitorComparisonResultDto;
import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final CompetitorComparisonApplicationService competitorComparisonApplicationService;

    public CompetitorController(CompetitorAnalysisApplicationService competitorAnalysisApplicationService,
                                 CompetitorComparisonApplicationService competitorComparisonApplicationService) {
        this.competitorAnalysisApplicationService = competitorAnalysisApplicationService;
        this.competitorComparisonApplicationService = competitorComparisonApplicationService;
    }

    @Operation(summary = "競合アカウント統計取得",
            description = "平均いいね数・平均コメント数・投稿頻度・投稿時間傾向・平均再生数・投稿形式分布・"
                    + "ジャンル分布・伸びる投稿ランキング等を返す")
    @GetMapping("/{accountId}/stats")
    public ResponseEntity<CompetitorStatsDto> getStats(@PathVariable UUID accountId) {
        return ResponseEntity.ok(competitorAnalysisApplicationService.getStats(accountId));
    }

    @Operation(summary = "競合アカウントとの差分をAIが説明（AIマーケティングOS Phase9）",
            description = "自社(accountId)と競合(competitorAccountId)の統計を比較し、AIが差分を自然言語で説明する。")
    @GetMapping("/{accountId}/compare/{competitorAccountId}")
    public ResponseEntity<CompetitorComparisonResultDto> compare(Authentication authentication,
                                                                   @PathVariable UUID accountId,
                                                                   @PathVariable UUID competitorAccountId) {
        return ResponseEntity.ok(competitorComparisonApplicationService.compare(
                accountId, competitorAccountId, currentUserId(authentication)));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
