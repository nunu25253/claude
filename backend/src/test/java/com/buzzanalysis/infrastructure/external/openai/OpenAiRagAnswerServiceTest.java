package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.rag.dto.RagDocumentDto;
import com.buzzanalysis.domain.rag.RagSourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenAiRagAnswerServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private RagDocumentDto sampleDocument() {
        return new RagDocumentDto(UUID.randomUUID(), RagSourceType.EVALUATION, UUID.randomUUID(),
                "過去の投稿評価: フックが弱く改善が必要だった", OffsetDateTime.now());
    }

    @Test
    void generateAnswer_returnsNoDataMessage_whenContextEmpty() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiRagAnswerService service = new OpenAiRagAnswerService(openAiClient, properties);

        String result = service.generateAnswer("質問", List.of());

        assertThat(result).contains("先にRAG索引登録");
        verifyNoInteractions(openAiClient);
    }

    @Test
    void generateAnswer_returnsFallback_whenApiKeyNotConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        OpenAiRagAnswerService service = new OpenAiRagAnswerService(openAiClient, properties);

        String result = service.generateAnswer("質問", List.of(sampleDocument()));

        assertThat(result).contains("抽出型簡易回答").contains("フックが弱く");
        verifyNoInteractions(openAiClient);
    }

    @Test
    void generateAnswer_usesAiResponse_whenConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiRagAnswerService service = new OpenAiRagAnswerService(openAiClient, properties);
        when(openAiClient.chatCompleteAsPlainText(any(), any())).thenReturn("AI生成回答");

        String result = service.generateAnswer("質問", List.of(sampleDocument()));

        assertThat(result).isEqualTo("AI生成回答");
    }

    @Test
    void generateAnswer_returnsFallback_whenApiCallThrows() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiRagAnswerService service = new OpenAiRagAnswerService(openAiClient, properties);
        when(openAiClient.chatCompleteAsPlainText(any(), any())).thenThrow(new RuntimeException("boom"));

        String result = service.generateAnswer("質問", List.of(sampleDocument()));

        assertThat(result).contains("抽出型簡易回答");
    }
}
