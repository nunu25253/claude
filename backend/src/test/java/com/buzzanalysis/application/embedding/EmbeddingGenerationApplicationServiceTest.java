package com.buzzanalysis.application.embedding;

import com.buzzanalysis.application.embedding.dto.EmbeddingGenerationSummaryDto;
import com.buzzanalysis.application.preprocessing.PreprocessingApplicationService;
import com.buzzanalysis.application.preprocessing.dto.PreprocessedPostDto;
import com.buzzanalysis.domain.embedding.Embedding;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingRepository;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import com.buzzanalysis.domain.preprocessing.ContentFormat;
import com.buzzanalysis.domain.preprocessing.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmbeddingGenerationApplicationServiceTest {

    @Mock
    private PreprocessingApplicationService preprocessingApplicationService;
    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private EmbeddingRepository embeddingRepository;

    private EmbeddingGenerationApplicationService service;

    @BeforeEach
    void setUp() {
        service = new EmbeddingGenerationApplicationService(
                preprocessingApplicationService, embeddingClient, embeddingRepository);
    }

    @Test
    void generateForPost_generatesBodyAndHashtags_whenNoExistingEmbeddings() {
        UUID postId = UUID.randomUUID();
        when(preprocessingApplicationService.preprocess(postId)).thenReturn(preprocessed(postId, "本文テキスト", List.of("tag1", "tag2")));
        when(embeddingRepository.findByPostIdAndTarget(any(), any())).thenReturn(Optional.empty());
        when(embeddingClient.embed(any())).thenReturn(new EmbeddingResult(new float[]{0.1f, 0.2f}, "text-embedding-3-small", 2));
        when(embeddingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmbeddingGenerationSummaryDto summary = service.generateForPost(postId);

        assertThat(summary.generated()).containsExactlyInAnyOrder(EmbeddingTarget.BODY, EmbeddingTarget.HASHTAGS);
        assertThat(summary.skipped()).isEmpty();
        verify(embeddingClient, org.mockito.Mockito.times(2)).embed(any());
    }

    @Test
    void generateForPost_skipsUnchangedTarget_toAvoidRedundantApiCost() {
        UUID postId = UUID.randomUUID();
        when(preprocessingApplicationService.preprocess(postId)).thenReturn(preprocessed(postId, "本文テキスト", List.of()));
        Embedding existingBody = new Embedding(UUID.randomUUID(), postId, EmbeddingTarget.BODY,
                new float[]{0.9f}, "本文テキスト", "text-embedding-3-small", 1, OffsetDateTime.now());
        when(embeddingRepository.findByPostIdAndTarget(postId, EmbeddingTarget.BODY)).thenReturn(Optional.of(existingBody));

        EmbeddingGenerationSummaryDto summary = service.generateForPost(postId);

        assertThat(summary.skipped()).containsExactly(EmbeddingTarget.BODY);
        assertThat(summary.generated()).isEmpty();
        verify(embeddingClient, never()).embed(any());
    }

    @Test
    void generateForPost_regeneratesTarget_whenSourceTextChanged() {
        UUID postId = UUID.randomUUID();
        when(preprocessingApplicationService.preprocess(postId)).thenReturn(preprocessed(postId, "更新後の本文", List.of()));
        Embedding existingBody = new Embedding(UUID.randomUUID(), postId, EmbeddingTarget.BODY,
                new float[]{0.9f}, "更新前の本文", "text-embedding-3-small", 1, OffsetDateTime.now());
        when(embeddingRepository.findByPostIdAndTarget(postId, EmbeddingTarget.BODY)).thenReturn(Optional.of(existingBody));
        when(embeddingClient.embed("更新後の本文")).thenReturn(new EmbeddingResult(new float[]{0.5f}, "text-embedding-3-small", 1));
        when(embeddingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmbeddingGenerationSummaryDto summary = service.generateForPost(postId);

        assertThat(summary.generated()).containsExactly(EmbeddingTarget.BODY);
        verify(embeddingClient).embed("更新後の本文");
    }

    @Test
    void generateForPost_skipsHashtagsTarget_whenNoHashtagsPresent() {
        UUID postId = UUID.randomUUID();
        when(preprocessingApplicationService.preprocess(postId)).thenReturn(preprocessed(postId, "本文テキスト", List.of()));
        when(embeddingRepository.findByPostIdAndTarget(any(), any())).thenReturn(Optional.empty());
        when(embeddingClient.embed(any())).thenReturn(new EmbeddingResult(new float[]{0.1f}, "text-embedding-3-small", 1));
        when(embeddingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmbeddingGenerationSummaryDto summary = service.generateForPost(postId);

        assertThat(summary.generated()).containsExactly(EmbeddingTarget.BODY);
        verify(embeddingClient, org.mockito.Mockito.times(1)).embed(any());
    }

    @Test
    void resolveSourceText_throwsForTitleAndCommentSummary() {
        PreprocessedPostDto dto = preprocessed(UUID.randomUUID(), "text", List.of());

        assertThatThrownBy(() -> service.resolveSourceText(dto, EmbeddingTarget.TITLE))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("TITLE");
        assertThatThrownBy(() -> service.resolveSourceText(dto, EmbeddingTarget.COMMENT_SUMMARY))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("COMMENT_SUMMARY");
    }

    private PreprocessedPostDto preprocessed(UUID postId, String cleanText, List<String> hashtags) {
        return new PreprocessedPostDto(postId, cleanText, Language.JAPANESE, hashtags, List.of(),
                null, null, ContentFormat.TEXT_ONLY);
    }
}
