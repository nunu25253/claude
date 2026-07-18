package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.application.script.AiScriptGenerationPort;
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
class OpenAiScriptGenerationServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private ContentProposalDto sampleProposal() {
        return new ContentProposalDto(UUID.randomUUID(), UUID.randomUUID(), 1, "企画タイトル", "フック",
                "構成", "CTA", "ターゲット", "美容", ContentFormat.SHORT_VIDEO, "理由", OffsetDateTime.now());
    }

    @Test
    void generate_returnsFallback_whenApiKeyNotConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        OpenAiScriptGenerationService service = new OpenAiScriptGenerationService(openAiClient, properties, objectMapper);

        AiScriptGenerationPort.GeneratedScript result = service.generate(sampleProposal(), 30);

        assertThat(result.cuts()).hasSize(3);
        assertThat(result.cuts().get(0).startSecond()).isEqualTo(0);
        assertThat(result.cuts().get(2).endSecond()).isEqualTo(30);
        verifyNoInteractions(openAiClient);
    }

    @Test
    void generate_parsesValidCuts_andExcludesInvalidRanges() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiScriptGenerationService service = new OpenAiScriptGenerationService(openAiClient, properties, objectMapper);
        String json = """
                {"bgmImage": "アップテンポ", "callToAction": "保存してね",
                 "cuts": [
                   {"cutNumber": 2, "startSecond": 5, "endSecond": 15, "narration": "本編", "telop": "本編テロップ", "visualDirection": "寄り"},
                   {"cutNumber": 1, "startSecond": 0, "endSecond": 5, "narration": "フック", "telop": "フックテロップ", "visualDirection": "引き"},
                   {"cutNumber": 3, "startSecond": 10, "endSecond": 40, "narration": "尺超過は除外される", "telop": "x", "visualDirection": "x"},
                   {"cutNumber": 4, "startSecond": 20, "endSecond": 10, "narration": "逆転は除外される", "telop": "x", "visualDirection": "x"}
                 ]}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        AiScriptGenerationPort.GeneratedScript result = service.generate(sampleProposal(), 30);

        assertThat(result.bgmImage()).isEqualTo("アップテンポ");
        assertThat(result.cuts()).hasSize(2);
        assertThat(result.cuts().get(0).cutNumber()).isEqualTo(1);
        assertThat(result.cuts().get(1).cutNumber()).isEqualTo(2);
    }

    @Test
    void generate_fallsBackToThreeCuts_whenAllParsedCutsInvalid() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiScriptGenerationService service = new OpenAiScriptGenerationService(openAiClient, properties, objectMapper);
        String json = """
                {"bgmImage": "無視される", "callToAction": "無視される",
                 "cuts": [
                   {"cutNumber": 1, "startSecond": 100, "endSecond": 200, "narration": "尺超過", "telop": "x", "visualDirection": "x"}
                 ]}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        AiScriptGenerationPort.GeneratedScript result = service.generate(sampleProposal(), 60);

        assertThat(result.cuts()).hasSize(3);
        assertThat(result.cuts().get(2).endSecond()).isEqualTo(60);
    }

    @Test
    void generate_returnsFallback_whenApiCallThrows() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiScriptGenerationService service = new OpenAiScriptGenerationService(openAiClient, properties, objectMapper);
        when(openAiClient.chatComplete(any(), any())).thenThrow(new RuntimeException("boom"));

        AiScriptGenerationPort.GeneratedScript result = service.generate(sampleProposal(), 90);

        assertThat(result.cuts()).hasSize(3);
        assertThat(result.cuts().get(2).endSecond()).isEqualTo(90);
    }
}
