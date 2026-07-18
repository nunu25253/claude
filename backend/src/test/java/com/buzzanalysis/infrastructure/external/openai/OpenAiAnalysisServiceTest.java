package com.buzzanalysis.infrastructure.external.openai;

import com.buzzanalysis.application.post.AiPostAnalysisPort;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link OpenAiAnalysisService} の単体テスト。APIキー未設定時のルールベースフォールバック分析を検証する
 * （Phase5で追加されたgenre/subGenre/postPurpose/postStructureAnalysis/strengths/weaknessesを含む）。
 */
@ExtendWith(MockitoExtension.class)
class OpenAiAnalysisServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private OpenAiAnalysisService service;

    @BeforeEach
    void setUp() {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey(""); // 未設定 -> フォールバック
        service = new OpenAiAnalysisService(openAiClient, properties, new ObjectMapper());
    }

    @Test
    void analyze_returnsFallbackAnalysis_includingPhase5Fields() {
        Post post = post(List.of("美容", "スキンケア"));

        AiPostAnalysisPort.AiAnalysisOutput result = service.analyze(post);

        assertThat(result.genre()).contains("美容");
        assertThat(result.subGenre()).isEqualTo("スキンケア");
        assertThat(result.postPurpose()).isNotBlank();
        assertThat(result.postStructureAnalysis()).isNotBlank();
        assertThat(result.strengths()).isNotBlank();
        assertThat(result.weaknesses()).isNotBlank();
        // 既存項目も引き続き埋まっていることを確認(後方互換)
        assertThat(result.hook()).isNotBlank();
        assertThat(result.callToAction()).isNotBlank();
        assertThat(result.improvementSuggestions()).isNotBlank();
    }

    @Test
    void analyze_usesPlaceholderGenre_whenNoHashtags() {
        Post post = post(List.of());

        AiPostAnalysisPort.AiAnalysisOutput result = service.analyze(post);

        assertThat(result.genre()).contains("未分類");
        assertThat(result.subGenre()).isEqualTo("-");
    }

    private Post post(List<String> hashtags) {
        return new Post(UUID.randomUUID(), UUID.randomUUID(), Platform.INSTAGRAM, "ext-1",
                "https://instagram.com/p/1", OffsetDateTime.now(), "creator", "caption text", hashtags,
                1000L, 100L, 20000L, 30L, 15, null, PostType.REEL, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
