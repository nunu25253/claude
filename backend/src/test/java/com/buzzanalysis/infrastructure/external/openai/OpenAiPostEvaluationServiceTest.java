package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.evaluation.AiPostEvaluationPort;
import com.buzzanalysis.application.proposal.dto.ContentProposalDto;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiPostEvaluationServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    private AiPostEvaluationPort.EvaluationTarget sampleTarget() {
        return new AiPostEvaluationPort.EvaluationTarget("タイトル", "フック", "構成", "CTA", "ターゲット");
    }

    private ContentProposalDto sampleProposal() {
        return new ContentProposalDto(UUID.randomUUID(), UUID.randomUUID(), 1, "企画タイトル", "フック",
                "構成", "CTA", "ターゲット", "美容", ContentFormat.SHORT_VIDEO, "理由", OffsetDateTime.now());
    }

    @Test
    void evaluate_returnsFallback_whenApiKeyNotConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        OpenAiPostEvaluationService service = new OpenAiPostEvaluationService(openAiClient, properties, objectMapper);

        AiPostEvaluationPort.AiEvaluationOutput result = service.evaluate(sampleTarget(), Optional.empty());

        assertThat(result.predictedScore()).isBetween(0, 100);
        assertThat(result.matchRatePercent()).isNull();
        verifyNoInteractions(openAiClient);
    }

    @Test
    void evaluate_returnsNullMatchRate_whenNoReferenceProposal_evenIfAiOmitsIt() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiPostEvaluationService service = new OpenAiPostEvaluationService(openAiClient, properties, objectMapper);
        String json = """
                {"targetAudienceEstimate": "20代女性", "improvementSuggestions": ["フックを強くする"],
                 "hookImprovement": "改善案", "ctaImprovement": "改善案2", "predictedScore": 70}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        AiPostEvaluationPort.AiEvaluationOutput result = service.evaluate(sampleTarget(), Optional.empty());

        assertThat(result.matchRatePercent()).isNull();
        assertThat(result.predictedScore()).isEqualTo(70);
        assertThat(result.improvementSuggestions()).containsExactly("フックを強くする");
    }

    @Test
    void evaluate_returnsMatchRate_whenReferenceProposalProvided() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiPostEvaluationService service = new OpenAiPostEvaluationService(openAiClient, properties, objectMapper);
        String json = """
                {"matchRatePercent": 85.5, "targetAudienceEstimate": "20代女性",
                 "improvementSuggestions": [], "hookImprovement": "改善案", "ctaImprovement": "改善案2",
                 "predictedScore": 60}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        AiPostEvaluationPort.AiEvaluationOutput result = service.evaluate(sampleTarget(), Optional.of(sampleProposal()));

        assertThat(result.matchRatePercent()).isEqualTo(85.5);
    }

    @Test
    void evaluate_clampsPredictedScoreToValidRange() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiPostEvaluationService service = new OpenAiPostEvaluationService(openAiClient, properties, objectMapper);
        String json = """
                {"targetAudienceEstimate": "x", "improvementSuggestions": [], "hookImprovement": "x",
                 "ctaImprovement": "x", "predictedScore": 150}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        AiPostEvaluationPort.AiEvaluationOutput result = service.evaluate(sampleTarget(), Optional.empty());

        assertThat(result.predictedScore()).isEqualTo(100);
    }

    @Test
    void evaluate_returnsFallback_whenApiCallThrows() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiPostEvaluationService service = new OpenAiPostEvaluationService(openAiClient, properties, objectMapper);
        when(openAiClient.chatComplete(any(), any())).thenThrow(new RuntimeException("boom"));

        AiPostEvaluationPort.AiEvaluationOutput result = service.evaluate(sampleTarget(), Optional.empty());

        assertThat(result.improvementSuggestions().get(0)).contains("ヒューリスティック簡易評価");
    }
}
