package com.buzzanalysis.application.semanticsearch;

import com.buzzanalysis.application.semanticsearch.dto.SemanticSearchResultDto;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingRepository;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import com.buzzanalysis.domain.embedding.SimilarityMatch;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SemanticSearchApplicationService}（Phase4: 意味検索エンジン）のMockitoによる単体テスト。
 */
@ExtendWith(MockitoExtension.class)
class SemanticSearchApplicationServiceTest {

    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private EmbeddingRepository embeddingRepository;
    @Mock
    private PostRepository postRepository;

    private SemanticSearchApplicationService service;

    @BeforeEach
    void setUp() {
        service = new SemanticSearchApplicationService(embeddingClient, embeddingRepository, postRepository);
    }

    @Test
    void search_embedsKeywordAndReturnsPostsWithMatchRate() {
        float[] queryVector = {0.1f, 0.2f};
        when(embeddingClient.embed("楽天カード")).thenReturn(new EmbeddingResult(queryVector, "text-embedding-3-small", 2));

        UUID postId = UUID.randomUUID();
        when(embeddingRepository.findNearest(EmbeddingTarget.BODY, queryVector, 20))
                .thenReturn(List.of(new SimilarityMatch(postId, 0.87)));
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost(postId)));

        List<SemanticSearchResultDto> results = service.search("楽天カード", 20);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).similarity()).isEqualTo(0.87);
        assertThat(results.get(0).matchRatePercent()).isEqualTo(87.0);
        assertThat(results.get(0).post().id()).isEqualTo(postId);
        verify(embeddingRepository).findNearest(EmbeddingTarget.BODY, queryVector, 20);
    }

    @Test
    void search_clampsMatchRateTo100_whenSimilarityExceedsOne() {
        // 浮動小数点誤差でわずかに1.0を超えるケースを想定
        when(embeddingClient.embed("keyword")).thenReturn(new EmbeddingResult(new float[]{1.0f}, "model", 1));
        UUID postId = UUID.randomUUID();
        when(embeddingRepository.findNearest(any(), any(), anyInt())).thenReturn(List.of(new SimilarityMatch(postId, 1.0001)));
        when(postRepository.findById(postId)).thenReturn(Optional.of(samplePost(postId)));

        List<SemanticSearchResultDto> results = service.search("keyword", 5);

        assertThat(results.get(0).matchRatePercent()).isEqualTo(100.0);
    }

    @Test
    void search_skipsMatch_whenPostNoLongerExists() {
        when(embeddingClient.embed("keyword")).thenReturn(new EmbeddingResult(new float[]{0.5f}, "model", 1));
        UUID postId = UUID.randomUUID();
        when(embeddingRepository.findNearest(any(), any(), anyInt())).thenReturn(List.of(new SimilarityMatch(postId, 0.5)));
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        List<SemanticSearchResultDto> results = service.search("keyword", 5);

        assertThat(results).isEmpty();
    }

    private Post samplePost(UUID postId) {
        return new Post(postId, UUID.randomUUID(), Platform.INSTAGRAM, "ext-1", "https://instagram.com/p/1",
                OffsetDateTime.now(), "creator", "caption", List.of("tag"), 10L, 2L, 100L, null, null, null,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
