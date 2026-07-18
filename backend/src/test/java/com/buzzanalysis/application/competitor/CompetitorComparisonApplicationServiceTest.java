package com.buzzanalysis.application.competitor;

import com.buzzanalysis.application.competitor.dto.CompetitorComparisonResultDto;
import com.buzzanalysis.application.competitor.dto.CompetitorStatsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitorComparisonApplicationServiceTest {

    @Mock
    private CompetitorAnalysisApplicationService competitorAnalysisApplicationService;
    @Mock
    private AiCompetitorDifferencePort aiCompetitorDifferencePort;

    private CompetitorComparisonApplicationService service;

    @BeforeEach
    void setUp() {
        service = new CompetitorComparisonApplicationService(competitorAnalysisApplicationService, aiCompetitorDifferencePort);
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

        CompetitorComparisonResultDto result = service.compare(accountId, competitorId);

        assertThat(result.target()).isEqualTo(targetStats);
        assertThat(result.competitor()).isEqualTo(competitorStats);
        assertThat(result.aiExplanation()).isEqualTo("説明文");
    }
}
