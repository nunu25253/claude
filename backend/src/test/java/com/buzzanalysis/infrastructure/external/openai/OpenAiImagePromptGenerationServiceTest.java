package com.buzzanalysis.infrastructure.external.openai;

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
class OpenAiImagePromptGenerationServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void generate_returnsEmptyList_whenInputEmpty() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiImagePromptGenerationService service = new OpenAiImagePromptGenerationService(openAiClient, properties, objectMapper);

        List<String> result = service.generate(List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(openAiClient);
    }

    @Test
    void generate_returnsFallbackForAll_whenApiKeyNotConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        OpenAiImagePromptGenerationService service = new OpenAiImagePromptGenerationService(openAiClient, properties, objectMapper);

        List<String> result = service.generate(List.of("フック導入", "本編"));

        assertThat(result).hasSize(2);
        assertThat(result.get(0)).contains("フック導入").contains("ルールベース簡易生成");
        verifyNoInteractions(openAiClient);
    }

    @Test
    void generate_usesAiPrompts_whenCountMatches() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiImagePromptGenerationService service = new OpenAiImagePromptGenerationService(openAiClient, properties, objectMapper);
        String json = """
                {"prompts": ["生成プロンプト1", "生成プロンプト2"]}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        List<String> result = service.generate(List.of("方向性1", "方向性2"));

        assertThat(result).containsExactly("生成プロンプト1", "生成プロンプト2");
    }

    @Test
    void generate_fallsBackOnlyForMissingOrBlankElements() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiImagePromptGenerationService service = new OpenAiImagePromptGenerationService(openAiClient, properties, objectMapper);
        String json = """
                {"prompts": ["生成プロンプト1", ""]}
                """;
        when(openAiClient.chatComplete(any(), any())).thenReturn(json);

        List<String> result = service.generate(List.of("方向性1", "方向性2", "方向性3"));

        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isEqualTo("生成プロンプト1");
        assertThat(result.get(1)).contains("方向性2").contains("ルールベース簡易生成");
        assertThat(result.get(2)).contains("方向性3").contains("ルールベース簡易生成");
    }

    @Test
    void generate_returnsFallbackForAll_whenApiCallThrows() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiImagePromptGenerationService service = new OpenAiImagePromptGenerationService(openAiClient, properties, objectMapper);
        when(openAiClient.chatComplete(any(), any())).thenThrow(new RuntimeException("boom"));

        List<String> result = service.generate(List.of("方向性1"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).contains("方向性1").contains("ルールベース簡易生成");
    }
}
