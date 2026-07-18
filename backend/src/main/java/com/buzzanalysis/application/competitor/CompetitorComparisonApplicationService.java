package com.buzzanalysis.application.competitor;

import com.buzzanalysis.application.competitor.dto.CompetitorComparisonResultDto;
import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 「競合比較」ユースケース（Phase9）。既存の{@link CompetitorAnalysisApplicationService#getStats}
 * （Redisキャッシュ済み）を再利用して自社・競合双方の統計を取得し、AIによる差分説明を付与する。
 */
@Service
public class CompetitorComparisonApplicationService {

    private final CompetitorAnalysisApplicationService competitorAnalysisApplicationService;
    private final AiCompetitorDifferencePort aiCompetitorDifferencePort;

    public CompetitorComparisonApplicationService(CompetitorAnalysisApplicationService competitorAnalysisApplicationService,
                                                   AiCompetitorDifferencePort aiCompetitorDifferencePort) {
        this.competitorAnalysisApplicationService = competitorAnalysisApplicationService;
        this.aiCompetitorDifferencePort = aiCompetitorDifferencePort;
    }

    public CompetitorComparisonResultDto compare(UUID accountId, UUID competitorAccountId) {
        CompetitorStatsDto target = competitorAnalysisApplicationService.getStats(accountId);
        CompetitorStatsDto competitor = competitorAnalysisApplicationService.getStats(competitorAccountId);
        String explanation = aiCompetitorDifferencePort.explainDifference(target, competitor);
        return new CompetitorComparisonResultDto(target, competitor, explanation);
    }
}
