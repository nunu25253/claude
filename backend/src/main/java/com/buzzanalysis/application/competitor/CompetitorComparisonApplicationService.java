package com.buzzanalysis.application.competitor;

import com.buzzanalysis.application.competitor.dto.CompetitorComparisonResultDto;
import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
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
    private final UsageQuotaService usageQuotaService;

    public CompetitorComparisonApplicationService(CompetitorAnalysisApplicationService competitorAnalysisApplicationService,
                                                   AiCompetitorDifferencePort aiCompetitorDifferencePort,
                                                   UsageQuotaService usageQuotaService) {
        this.competitorAnalysisApplicationService = competitorAnalysisApplicationService;
        this.aiCompetitorDifferencePort = aiCompetitorDifferencePort;
        this.usageQuotaService = usageQuotaService;
    }

    public CompetitorComparisonResultDto compare(UUID accountId, UUID competitorAccountId, UUID requestingUserId) {
        if (!usageQuotaService.tryConsume(requestingUserId)) {
            throw new BusinessRuleViolationException("本日のAI機能の利用回数上限に達しました。明日以降に再度お試しください。",
                    UsageQuotaService.EXCEEDED_ERROR_CODE);
        }
        CompetitorStatsDto target = competitorAnalysisApplicationService.getStats(accountId);
        CompetitorStatsDto competitor = competitorAnalysisApplicationService.getStats(competitorAccountId);
        String explanation = aiCompetitorDifferencePort.explainDifference(target, competitor);
        return new CompetitorComparisonResultDto(target, competitor, explanation);
    }
}
