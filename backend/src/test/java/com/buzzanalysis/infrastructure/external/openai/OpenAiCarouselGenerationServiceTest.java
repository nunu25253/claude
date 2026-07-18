package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.carousel.AiCarouselGenerationPort;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiCarouselGenerationServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private ContentProposalDto sampleProposal() {
        return new ContentProposalDto(UUID.randomUUID(), UUID.randomUUID(), 1, "企画タイトル", "フック",
                "構成", "CTA", "ターゲット", "美容", ContentFormat.MULTI_IMAGE_CAROUSEL, "理由", OffsetDateTime.now());
    }

    @Test
    void generate_returnsFallback_whenApiKeyNotConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        OpenAiCarouselGenerationService service = new OpenAiCarouselGenerationService(openAiClient, properties, objectMapper);

        AiCarouselGenerationPort.GeneratedCarousel result = service.generate(sampleProposal());

        assertThat(result.pages()).hasSize(3);
        verifyNoInteractions(openAiClient);
    }

    @Test
    void generate_parsesPages_andSkipsPageWithoutHeadline() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiCarouselGenerationService service = new OpenAiCarouselGenerationService(openAiClient, properties, objectMapper);
        String json = """
                {"pages": [
                  {"headline": "1ページ目", "bodyText": "本文1", "visualDirection": "指示1"},
                  {"bodyText": "見出しなしは除外される"},
                  {"headline": "3ページ目", "bodyText": "本文3", "visualDirection": "指示3"}
                ]}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        AiCarouselGenerationPort.GeneratedCarousel result = service.generate(sampleProposal());

        assertThat(result.pages()).hasSize(2);
        assertThat(result.pages().get(0).headline()).isEqualTo("1ページ目");
        assertThat(result.pages().get(1).headline()).isEqualTo("3ページ目");
    }

    @Test
    void generate_truncatesToMaxEightPages() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiCarouselGenerationService service = new OpenAiCarouselGenerationService(openAiClient, properties, objectMapper);
        StringBuilder pagesJson = new StringBuilder();
        for (int i = 1; i <= 12; i++) {
            if (i > 1) {
                pagesJson.append(",");
            }
            pagesJson.append("{\"headline\": \"ページ").append(i).append("\"}");
        }
        String json = "{\"pages\": [" + pagesJson + "]}";
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        AiCarouselGenerationPort.GeneratedCarousel result = service.generate(sampleProposal());

        assertThat(result.pages()).hasSize(8);
    }

    @Test
    void generate_fallsBack_whenFewerThanTwoValidPages() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiCarouselGenerationService service = new OpenAiCarouselGenerationService(openAiClient, properties, objectMapper);
        String json = """
                {"pages": [
                  {"headline": "1ページのみ"}
                ]}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        AiCarouselGenerationPort.GeneratedCarousel result = service.generate(sampleProposal());

        assertThat(result.pages()).hasSize(3);
    }

    @Test
    void generate_returnsFallback_whenApiCallThrows() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiCarouselGenerationService service = new OpenAiCarouselGenerationService(openAiClient, properties, objectMapper);
        when(openAiClient.chatComplete(any(), any())).thenThrow(new RuntimeException("boom"));

        AiCarouselGenerationPort.GeneratedCarousel result = service.generate(sampleProposal());

        assertThat(result.pages()).hasSize(3);
        assertThat(result.pages().get(0).visualDirection()).contains("ルールベース簡易生成");
    }
}
