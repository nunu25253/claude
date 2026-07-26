package com.buzzanalysis.application.rankingscore;

import com.buzzanalysis.application.matching.UserConditionMatchApplicationService;
import com.buzzanalysis.application.matching.dto.MatchRateResultDto;
import com.buzzanalysis.application.post.dto.PostDto;
import com.buzzanalysis.application.rankingscore.dto.RankingScoreResultDto;
import com.buzzanalysis.domain.analysis.AnalysisResultRepository;
import com.buzzanalysis.domain.matching.UserSearchCondition;
import com.buzzanalysis.domain.normalization.NormalizedPost;
import com.buzzanalysis.domain.normalization.PostNormalizer;
import com.buzzanalysis.domain.platform.Platform;
import com.buzzanalysis.domain.post.Post;
import com.buzzanalysis.domain.post.PostRepository;
import com.buzzanalysis.domain.post.PostType;
import com.buzzanalysis.domain.preprocessing.PostPreprocessor;
import com.buzzanalysis.domain.preprocessing.PreprocessedPost;
import com.buzzanalysis.domain.rankingscore.RankingScoreCalculator;
import com.buzzanalysis.domain.score.BuzzScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContentRankingApplicationServiceTest {

    @Mock
    private UserConditionMatchApplicationService userConditionMatchApplicationService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private AnalysisResultRepository analysisResultRepository;
    @Mock
    private BuzzScoreRepository buzzScoreRepository;
    @Mock
    private PostNormalizer postNormalizer;
    @Mock
    private PostPreprocessor postPreprocessor;
    @Mock
    private RankingScoreCalculator rankingScoreCalculator;

    private ContentRankingApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ContentRankingApplicationService(userConditionMatchApplicationService, postRepository,
                analysisResultRepository, buzzScoreRepository, postNormalizer, postPreprocessor, rankingScoreCalculator);
    }

    @Test
    void rank_scoresAndSortsCandidatesFromPhase6() {
        UUID lowId = UUID.randomUUID();
        UUID highId = UUID.randomUUID();
        Post lowPost = post(lowId);
        Post highPost = post(highId);
        UserSearchCondition condition = UserSearchCondition.builder().keyword("kw").build();

        when(userConditionMatchApplicationService.evaluate(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of(
                        new MatchRateResultDto(PostDto.from(lowPost), 40.0, Map.of()),
                        new MatchRateResultDto(PostDto.from(highPost), 90.0, Map.of())
                ));
        when(postRepository.findByIdIn(any())).thenReturn(List.of(lowPost, highPost));
        when(analysisResultRepository.findByPostIdIn(any())).thenReturn(List.of());
        when(buzzScoreRepository.findByPostIdIn(any())).thenReturn(List.of());
        when(postNormalizer.normalize(any())).thenReturn(NormalizedPost.builder().postId(UUID.randomUUID()).build());
        when(postPreprocessor.preprocess(any())).thenReturn(PreprocessedPost.builder().postId(UUID.randomUUID()).build());
        when(rankingScoreCalculator.calculate(argThat(input -> input != null && input.post().getId().equals(lowId))))
                .thenReturn(new RankingScoreCalculator.CalculationResult(35.0, Map.of()));
        when(rankingScoreCalculator.calculate(argThat(input -> input != null && input.post().getId().equals(highId))))
                .thenReturn(new RankingScoreCalculator.CalculationResult(95.0, Map.of()));

        List<RankingScoreResultDto> results = service.rank(condition, 10);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).post().id()).isEqualTo(highId);
        assertThat(results.get(0).rankingScore()).isEqualTo(95.0);
        assertThat(results.get(1).post().id()).isEqualTo(lowId);
    }

    @Test
    void rank_respectsLimit() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Post post1 = post(id1);
        Post post2 = post(id2);
        UserSearchCondition condition = UserSearchCondition.builder().build();

        when(userConditionMatchApplicationService.evaluate(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn(List.of(
                        new MatchRateResultDto(PostDto.from(post1), 50.0, Map.of()),
                        new MatchRateResultDto(PostDto.from(post2), 60.0, Map.of())
                ));
        when(postRepository.findByIdIn(any())).thenReturn(List.of(post1, post2));
        when(analysisResultRepository.findByPostIdIn(any())).thenReturn(List.of());
        when(buzzScoreRepository.findByPostIdIn(any())).thenReturn(List.of());
        when(postNormalizer.normalize(any())).thenReturn(NormalizedPost.builder().postId(UUID.randomUUID()).build());
        when(postPreprocessor.preprocess(any())).thenReturn(PreprocessedPost.builder().postId(UUID.randomUUID()).build());
        when(rankingScoreCalculator.calculate(any()))
                .thenReturn(new RankingScoreCalculator.CalculationResult(50.0, Map.of()));

        List<RankingScoreResultDto> results = service.rank(condition, 1);

        assertThat(results).hasSize(1);
    }

    private Post post(UUID id) {
        return new Post(id, UUID.randomUUID(), Platform.INSTAGRAM, "ext-" + id, "https://example.com/" + id,
                OffsetDateTime.now(), "creator", "caption", List.of(), 10L, 2L, 100L, null, null, null,
                PostType.IMAGE, OffsetDateTime.now(), OffsetDateTime.now());
    }

    private static <T> T argThat(org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
