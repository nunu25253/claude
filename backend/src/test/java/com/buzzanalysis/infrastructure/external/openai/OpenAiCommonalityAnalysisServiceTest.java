package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.commonality.AiCommonalityAnalysisPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OpenAiCommonalityAnalysisServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private OpenAiCommonalityAnalysisService service;

    @BeforeEach
    void setUp() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        service = new OpenAiCommonalityAnalysisService(openAiClient, properties, new ObjectMapper());
    }

    @Test
    void analyze_returnsNoDataMessage_whenSnippetsEmpty() {
        AiCommonalityAnalysisPort.AiCommonalityOutput result = service.analyze(List.of());

        assertThat(result.commonTitlePattern()).contains("分析結果が存在する投稿がない");
        verifyNoInteractions(openAiClient);
    }

    @Test
    void analyze_returnsFallback_whenApiKeyNotConfigured() {
        var snippet = new AiCommonalityAnalysisPort.PostSnippet("タイトルA", "フックA", "CTA A", "構成A", "ターゲットA");

        AiCommonalityAnalysisPort.AiCommonalityOutput result = service.analyze(List.of(snippet));

        assertThat(result.commonTitlePattern()).contains("タイトルA");
        assertThat(result.commonHookPattern()).contains("フックA");
        verifyNoInteractions(openAiClient);
    }
}
