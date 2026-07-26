package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.commonality.dto.CommonalityAnalysisResultDto;
import com.buzzanalysis.application.proposal.AiProposalGenerationPort;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiProposalGenerationServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private CommonalityAnalysisResultDto sampleCommonality() {
        return new CommonalityAnalysisResultDto(10, 5, List.of("#タグ"), 30, 20,
                ContentFormat.SHORT_VIDEO, "共通タイトル", "共通フック", "共通CTA", "共通構成", "共通ターゲット");
    }

    @Test
    void generate_returnsFallback_whenApiKeyNotConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        OpenAiProposalGenerationService service = new OpenAiProposalGenerationService(openAiClient, properties, objectMapper);

        List<AiProposalGenerationPort.GeneratedProposal> result = service.generate(sampleCommonality(), 3);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).title()).contains("共通タイトル");
        verifyNoInteractions(openAiClient);
    }

    @Test
    void generate_parsesProposalsArray_andTruncatesToRequestedCount() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiProposalGenerationService service = new OpenAiProposalGenerationService(openAiClient, properties, objectMapper);
        String json = """
                {"proposals": [
                  {"title": "企画1", "hookPattern": "フック1", "structureSummary": "構成1", "callToAction": "CTA1",
                   "targetAudience": "ターゲット1", "genre": "美容", "recommendedFormat": "SHORT_VIDEO", "reasoning": "理由1"},
                  {"title": "企画2", "recommendedFormat": "UNKNOWN_FORMAT"},
                  {"hookPattern": "タイトルなしは除外される"}
                ]}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        List<AiProposalGenerationPort.GeneratedProposal> result = service.generate(sampleCommonality(), 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("企画1");
        assertThat(result.get(0).recommendedFormat()).isEqualTo(ContentFormat.SHORT_VIDEO);
    }

    @Test
    void generate_skipsProposalWithoutTitle_andTreatsUnknownFormatAsNull() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiProposalGenerationService service = new OpenAiProposalGenerationService(openAiClient, properties, objectMapper);
        String json = """
                {"proposals": [
                  {"title": "企画A", "recommendedFormat": "NOT_A_REAL_FORMAT"},
                  {"hookPattern": "タイトル欠落"}
                ]}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        List<AiProposalGenerationPort.GeneratedProposal> result = service.generate(sampleCommonality(), 5);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("企画A");
        assertThat(result.get(0).recommendedFormat()).isNull();
    }

    @Test
    void generate_returnsFallback_whenApiCallThrows() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiProposalGenerationService service = new OpenAiProposalGenerationService(openAiClient, properties, objectMapper);
        when(openAiClient.chatComplete(any(), any())).thenThrow(new RuntimeException("boom"));

        List<AiProposalGenerationPort.GeneratedProposal> result = service.generate(sampleCommonality(), 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).reasoning()).contains("ルールベース簡易生成");
    }
}
