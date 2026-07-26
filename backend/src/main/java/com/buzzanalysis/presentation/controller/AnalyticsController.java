package com.buzzanalysis.presentation.controller;

import com.buzzanalysis.application.analytics.AiImprovementRateApplicationService;
import com.buzzanalysis.application.analytics.dto.AiImprovementRateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** プラットフォーム全体の統計情報API(個々のユーザーに紐づかない集計値)。 */
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = "Analytics", description = "プラットフォーム全体の統計情報")
public class AnalyticsController {

    private final AiImprovementRateApplicationService aiImprovementRateApplicationService;

    public AnalyticsController(AiImprovementRateApplicationService aiImprovementRateApplicationService) {
        this.aiImprovementRateApplicationService = aiImprovementRateApplicationService;
    }

    @Operation(summary = "AI提案の的中率取得",
            description = "初回分析からAIの改善提案を踏まえて再分析された投稿群のうち、実際にBuzzScoreが"
                    + "向上した割合を返す。集計対象のサンプルが無い場合はimprovedPercentage/averageScoreDeltaがnull。")
    @GetMapping("/ai-improvement-rate")
    public AiImprovementRateDto getAiImprovementRate() {
        return aiImprovementRateApplicationService.compute();
    }
}
