package com.buzzanalysis.infrastructure.external.openai;

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
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OpenAiCompetitorDifferenceServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private OpenAiCompetitorDifferenceService service;

    @BeforeEach
    void setUp() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        service = new OpenAiCompetitorDifferenceService(openAiClient, properties);
    }

    @Test
    void explainDifference_returnsFallback_highlightingHigherMetric() {
        CompetitorStatsDto target = new CompetitorStatsDto(UUID.randomUUID(), 200.0, 20.0, 5.0,
                Map.of(), 30.0, 50.0, List.of(), 1000.0, Map.of(), Map.of());
        CompetitorStatsDto competitor = new CompetitorStatsDto(UUID.randomUUID(), 100.0, 10.0, 3.0,
                Map.of(), 30.0, 50.0, List.of(), 500.0, Map.of(), Map.of());

        String explanation = service.explainDifference(target, competitor);

        assertThat(explanation).contains("平均いいね数 は競合より高い");
        verifyNoInteractions(openAiClient);
    }
}
