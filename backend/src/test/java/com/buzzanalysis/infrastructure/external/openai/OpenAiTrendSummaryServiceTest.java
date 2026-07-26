package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.domain.trend.TrendCategory;
import com.buzzanalysis.domain.trend.TrendItem;
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
class OpenAiTrendSummaryServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    @Test
    void summarize_returnsNoDataMessage_whenItemsEmpty() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiTrendSummaryService service = new OpenAiTrendSummaryService(openAiClient, properties);

        String result = service.summarize(List.of());

        assertThat(result).contains("検出されませんでした");
        verifyNoInteractions(openAiClient);
    }

    @Test
    void summarize_returnsFallback_whenApiKeyNotConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("");
        OpenAiTrendSummaryService service = new OpenAiTrendSummaryService(openAiClient, properties);
        TrendItem item = new TrendItem(TrendCategory.HASHTAG, "#美容", 10, 5, 100.0, false);

        String result = service.summarize(List.of(item));

        assertThat(result).contains("ルールベース簡易要約").contains("#美容");
        verifyNoInteractions(openAiClient);
    }

    @Test
    void summarize_usesAiResponse_whenConfigured() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiTrendSummaryService service = new OpenAiTrendSummaryService(openAiClient, properties);
        when(openAiClient.chatCompleteAsPlainText(any(), any())).thenReturn("AI生成サマリー");
        TrendItem item = new TrendItem(TrendCategory.GENRE, "美容", 8, 0, null, true);

        String result = service.summarize(List.of(item));

        assertThat(result).isEqualTo("AI生成サマリー");
    }

    @Test
    void summarize_returnsFallback_whenApiCallThrows() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey("sk-test");
        OpenAiTrendSummaryService service = new OpenAiTrendSummaryService(openAiClient, properties);
        when(openAiClient.chatCompleteAsPlainText(any(), any())).thenThrow(new RuntimeException("boom"));
        TrendItem item = new TrendItem(TrendCategory.CONTENT_FORMAT, "SHORT_VIDEO", 6, 3, 100.0, false);

        String result = service.summarize(List.of(item));

        assertThat(result).contains("ルールベース簡易要約");
    }
}
