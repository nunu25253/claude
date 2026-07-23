package com.buzzanalysis.application.competitor;

import com.buzzanalysis.application.competitor.dto.CompetitorComparisonResultDto;
import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import com.buzzanalysis.domain.common.exception.BusinessRuleViolationException;
import com.buzzanalysis.infrastructure.quota.UsageQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CompetitorComparisonApplicationServiceTest {

    @Mock
    private CompetitorAnalysisApplicationService competitorAnalysisApplicationService;
    @Mock
    private AiCompetitorDifferencePort aiCompetitorDifferencePort;
    @Mock
    private UsageQuotaService usageQuotaService;

    private CompetitorComparisonApplicationService service;
    private UUID requestingUserId;

    @BeforeEach
    void setUp() {
        service = new CompetitorComparisonApplicationService(competitorAnalysisApplicationService,
                aiCompetitorDifferencePort, usageQuotaService);
        requestingUserId = UUID.randomUUID();
        when(usageQuotaService.tryConsume(any())).thenReturn(true);
    }

    @Test
    void compare_throwsBusinessRuleViolation_whenDailyUsageQuotaExceeded() {
        when(usageQuotaService.tryConsume(requestingUserId)).thenReturn(false);

        assertThatThrownBy(() -> service.compare(UUID.randomUUID(), UUID.randomUUID(), requestingUserId))
                .isInstanceOf(BusinessRuleViolationException.class);
        verifyNoInteractions(competitorAnalysisApplicationService, aiCompetitorDifferencePort);
    }

    @Test
    void compare_fetchesBothAccountsStatsAndAttachesAiExplanation() {
        UUID accountId = UUID.randomUUID();
        UUID competitorId = UUID.randomUUID();
        CompetitorStatsDto targetStats = new CompetitorStatsDto(accountId, 100.0, 10.0, 3.0, Map.of(), null, 50.0,
                List.of(), null, Map.of(), Map.of());
        CompetitorStatsDto competitorStats = new CompetitorStatsDto(competitorId, 50.0, 5.0, 2.0, Map.of(), null,
                40.0, List.of(), null, Map.of(), Map.of());
        when(competitorAnalysisApplicationService.getStats(accountId)).thenReturn(targetStats);
        when(competitorAnalysisApplicationService.getStats(competitorId)).thenReturn(competitorStats);
        when(aiCompetitorDifferencePort.explainDifference(targetStats, competitorStats)).thenReturn("説明文");

        CompetitorComparisonResultDto result = service.compare(accountId, competitorId, requestingUserId);

        assertThat(result.target()).isEqualTo(targetStats);
        assertThat(result.competitor()).isEqualTo(competitorStats);
        assertThat(result.aiExplanation()).isEqualTo("説明文");
    }
}
