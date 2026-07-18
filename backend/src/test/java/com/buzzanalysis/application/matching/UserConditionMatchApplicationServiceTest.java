package com.buzzanalysis.application.matching;

import com.buzzanalysis.application.matching.dto.MatchRateResultDto;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.embedding.EmbeddingClient;
import com.buzzanalysis.domain.embedding.EmbeddingRepository;
import com.buzzanalysis.domain.embedding.EmbeddingResult;
import com.buzzanalysis.domain.embedding.EmbeddingTarget;
import com.buzzanalysis.domain.embedding.SimilarityMatch;
import com.buzzanalysis.domain.matching.MatchRateCalculator;
import com.buzzanalysis.domain.matching.UserSearchCondition;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostSearchResult;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserConditionMatchApplicationServiceTest {

    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private EmbeddingRepository embeddingRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private PostNormalizer postNormalizer;
    @Mock
    private PostPreprocessor postPreprocessor;
    @Mock
    private MatchRateCalculator matchRateCalculator;

    private UserConditionMatchApplicationService service;

    @BeforeEach
    void setUp() {
        service = new UserConditionMatchApplicationService(embeddingClient, embeddingRepository, postRepository,
                analysisResultRepository, postNormalizer, postPreprocessor, matchRateCalculator);
    }

    @Test
    void evaluate_usesSemanticSearch_whenKeywordSpecified() {
        UUID postId = UUID.randomUUID();
        UserSearchCondition condition = UserSearchCondition.builder().keyword("楽天カード").build();
        when(embeddingClient.embed("楽天カード")).thenReturn(new EmbeddingResult(new float[]{0.1f}, "model", 1));
        when(embeddingRepository.findNearest(eq(EmbeddingTarget.BODY), any(), anyInt()))
                .thenReturn(List.of(new SimilarityMatch(postId, 0.9)));
        Post post = post(postId, Platform.INSTAGRAM);
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.empty());
        when(postNormalizer.normalize(post)).thenReturn(NormalizedPost.builder().postId(postId).build());
        when(postPreprocessor.preprocess(any())).thenReturn(PreprocessedPost.builder().postId(postId).build());
        when(matchRateCalculator.calculate(any()))
                .thenReturn(new MatchRateCalculator.CalculationResult(77.0, Map.of()));

        List<MatchRateResultDto> results = service.evaluate(condition, 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).matchRatePercent()).isEqualTo(77.0);
        verify(postRepository, never()).search(any());
    }

    @Test
    void evaluate_usesPostSearch_whenNoFreeTextCondition() {
        UUID postId = UUID.randomUUID();
        UserSearchCondition condition = UserSearchCondition.builder().platform(Platform.TIKTOK).build();
        Post post = post(postId, Platform.TIKTOK);
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(post), 0, 50, 1));
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(analysisResultRepository.findByPostId(postId)).thenReturn(Optional.empty());
        when(postNormalizer.normalize(post)).thenReturn(NormalizedPost.builder().postId(postId).build());
        when(postPreprocessor.preprocess(any())).thenReturn(PreprocessedPost.builder().postId(postId).build());
        when(matchRateCalculator.calculate(any()))
                .thenReturn(new MatchRateCalculator.CalculationResult(60.0, Map.of()));

        List<MatchRateResultDto> results = service.evaluate(condition, 10);

        assertThat(results).hasSize(1);
        verify(embeddingClient, never()).embed(any());
    }

    @Test
    void evaluate_filtersOutMismatchedPlatform_fromSemanticCandidates() {
        UUID postId = UUID.randomUUID();
        UserSearchCondition condition = UserSearchCondition.builder().keyword("kw").platform(Platform.X).build();
        when(embeddingClient.embed("kw")).thenReturn(new EmbeddingResult(new float[]{0.1f}, "model", 1));
        when(embeddingRepository.findNearest(eq(EmbeddingTarget.BODY), any(), anyInt()))
                .thenReturn(List.of(new SimilarityMatch(postId, 0.9)));
        Post post = post(postId, Platform.INSTAGRAM); // 条件のプラットフォーム(X)と不一致
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        List<MatchRateResultDto> results = service.evaluate(condition, 10);

        assertThat(results).isEmpty();
    }

    @Test
    void evaluate_sortsByMatchRateDescending_andRespectsLimit() {
        UserSearchCondition condition = UserSearchCondition.builder().platform(Platform.INSTAGRAM).build();
        UUID lowId = UUID.randomUUID();
        UUID highId = UUID.randomUUID();
        Post lowPost = post(lowId, Platform.INSTAGRAM);
        Post highPost = post(highId, Platform.INSTAGRAM);
        when(postRepository.search(any())).thenReturn(new PostSearchResult(List.of(lowPost, highPost), 0, 50, 2));
        when(postRepository.findById(lowId)).thenReturn(Optional.of(lowPost));
        when(postRepository.findById(highId)).thenReturn(Optional.of(highPost));
        when(analysisResultRepository.findByPostId(any())).thenReturn(Optional.empty());
        when(postNormalizer.normalize(any())).thenReturn(NormalizedPost.builder().postId(UUID.randomUUID()).build());
        when(postPreprocessor.preprocess(any())).thenReturn(PreprocessedPost.builder().postId(UUID.randomUUID()).build());
        when(matchRateCalculator.calculate(argThat(input -> input != null && input.post().getId().equals(lowId))))
                .thenReturn(new MatchRateCalculator.CalculationResult(30.0, Map.of()));
        when(matchRateCalculator.calculate(argThat(input -> input != null && input.post().getId().equals(highId))))
                .thenReturn(new MatchRateCalculator.CalculationResult(90.0, Map.of()));

        List<MatchRateResultDto> results = service.evaluate(condition, 1);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).matchRatePercent()).isEqualTo(90.0);
    }

    private Post post(UUID id, Platform platform) {
        return new Post(id, UUID.randomUUID(), platform, "ext-" + id, "https://example.com/" + id,
                OffsetDateTime.now(), "creator", "caption", List.of(), 10L, 2L, 100L, null, null, null,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
    }
}
